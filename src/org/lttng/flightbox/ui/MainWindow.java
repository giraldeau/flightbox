package org.lttng.flightbox.ui;

import java.io.File;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.cpu.TraceEventHandlerStats;
import org.lttng.flightbox.io.EventQuery;
import org.lttng.flightbox.io.TraceReader;

public class MainWindow {

	class ExitListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Display display = e.display;
			display.close();
		}
		
	}

	private File traceDir;
	UsageStats cpuStats;
	CpuUsageView cpuView; 
	ProcessUsageView processView;
	Shell shell;
	
	public MainWindow() {
		Display display = new Display();
		shell = new Shell(display);
		shell.setText("Line Chart Example");
		shell.setSize(800, 800);
		shell.setLayout(new FillLayout());
		
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		shell.setLayout(layout);
		
		cpuView = new CpuUsageView(shell, SWT.BORDER);
		processView = new ProcessUsageView(shell, SWT.BORDER);
	
		class OpenListener extends SelectionAdapter {
			private MainWindow window;
			public OpenListener(MainWindow window) {
				this.window = window;
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
				dialog.setText("Open trace");
				String tracePath = dialog.open();
				if (tracePath != null) {
					File traceDir = new File(tracePath);
					File traceMetadata = new File(tracePath, "metadata_0");
					if (traceDir.isDirectory() && traceMetadata.isFile()) {
						window.setTraceDir(traceDir);
					}
				}
			}
		}
		
		//SettingsDialog settings = new SettingsDialog(shell);
		//settings.open();
		
		Menu menu = new Menu(shell, SWT.BAR);
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		
		MenuItem fileMenuItem = new MenuItem(menu, SWT.CASCADE);
		fileMenuItem.setText("&File");
		fileMenuItem.setMenu(fileMenu);
		
		MenuItem openMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		openMenuItem.setText("&Open");
		openMenuItem.setAccelerator(SWT.CTRL + 'O');
		openMenuItem.addSelectionListener(new OpenListener(this));
		MenuItem separator = new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText("E&xit");
		exitItem.setAccelerator(SWT.CTRL + 'Q');
		shell.setMenuBar(menu);
		
		exitItem.addSelectionListener(new ExitListener());
		
		// FIXME: Toolbar is below the window content and is hence hidden
		//ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL);
		//bar.setSize(shell.getSize().x, 65); // must be set in resize event
		//bar.setSize(300, 65);
		//bar.setLocation(0,0);
		//ToolItem exitToolItem = new ToolItem(bar, SWT.PUSH);
		//exitToolItem.setText("Exit");
		//exitToolItem.addSelectionListener(new ExitListener());
		//exitToolItem.setToolTipText("Exit the application");
		
		//Text text1 = new Text(shell, SWT.SINGLE | SWT.BORDER);
		//text1.setBounds(10,100,100,20);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();
	}

	public void setTraceDir(File traceDir) {
		this.traceDir = traceDir;
		// compute stats, update graphics
		EventQuery sched_query = new EventQuery();
		sched_query.addEventType("kernel");
		sched_query.addEventName("sched_schedule");
		TraceEventHandlerStats cpu_handler = new TraceEventHandlerStats();
		TraceReader reader = new TraceReader(this.traceDir.toString());
		reader.register(sched_query, cpu_handler);
		try {
			reader.process();
		} catch (JniException e) {
			e.printStackTrace();
			MessageBox msg = new MessageBox(shell);
			msg.setMessage("Error while reading the trace");
			msg.setText("Error");
			msg.open();
			return;
		}
		
		System.out.println(cpu_handler.getCpuUsageStats());
		
		cpuStats = cpu_handler.getCpuUsageStats();
		cpuView.setCpuStats(cpuStats);
		cpuView.update();
		
	}
	
}
