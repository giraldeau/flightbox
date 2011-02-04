package org.lttng.flightbox.ui;

import java.io.File;
import java.util.TreeMap;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.cpu.KernelProcess;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
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
	UsageStats<Long> cpuStats;
	CpuUsageView cpuView; 
	ProcessUsageView processView;
	Shell shell;
	Display display;
	
	public MainWindow() {
		this(new String[0]);
	}

	public MainWindow(String[] args) {
		
		display = new Display();
		shell = new Shell(display);
		shell.setText("Line Chart Example");
		shell.setSize(800, 800);
		shell.setLayout(new FillLayout());
		
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		shell.setLayout(layout);
		
		cpuView = new CpuUsageView(shell, SWT.BORDER);
		processView = new ProcessUsageView(shell, SWT.BORDER);
		cpuView.setProcessView(processView);
		processView.setCpuView(cpuView);
		
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
				
		if (args.length == 1) {
			setTraceDir(new File(args[0]));
		}
		
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
		
		TraceEventHandlerStats cpu_handler = new TraceEventHandlerStats();
		TraceEventHandlerProcess proc_handler = new TraceEventHandlerProcess();
		TraceReader reader = new TraceReader(this.traceDir.toString());
		reader.register(cpu_handler);
		reader.register(proc_handler);
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
				
		cpuStats = cpu_handler.getUsageStats();
		cpuView.setCpuStats(cpuStats);
		cpuView.updateData();
		cpuView.resetHighlight();

		UsageStats<Long> procStats = proc_handler.getUsageStats();
		TreeMap<Long, KernelProcess> procInfo = proc_handler.getProcInfo();
		processView.setStats(procStats, procInfo);
		processView.resetSumInterval();
	}
}
