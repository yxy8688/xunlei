import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import java.io.File;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import bean9.DownLoadManager;
import bean9.MyNotify;
import threadpool.ThreadPoolManager;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class XunLei {

	protected Shell shell;
	private Text text1;
	private Text text;
	private static ThreadPoolManager tpm;
	private ProgressBar progressBar;
	private static long fileSize=0;
	private DownLoadManager dlm;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		tpm=new ThreadPoolManager(5);
		try {
			XunLei window = new XunLei();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(485, 325);
		shell.setText("迅雷升级版");
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(33, 24, 61, 17);
		lblNewLabel.setText("文件地址：");
		
		text1 = new Text(shell, SWT.BORDER);
		text1.setText("http://dldir1.qq.com/qqfile/qq/QQ8.7/19113/QQ8.7.exe");
		text1.setBounds(99, 21, 342, 23);
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.setBounds(361, 125, 80, 27);
		btnNewButton.setText("选择目录");
		
		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.setBounds(99, 171, 80, 27);
		btnNewButton_1.setText("下   载");
		
		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setBounds(33, 80, 61, 17);
		lblNewLabel_1.setText("   线程数：");
		
		final Combo combo = new Combo(shell, SWT.NONE);
		combo.setItems(new String[] {"2", "4", "6", "8", "10"});
		combo.setBounds(99, 77, 88, 20);
		combo.select(2);
		
		Label lblNewLabel_2 = new Label(shell, SWT.NONE);
		lblNewLabel_2.setBounds(33, 127, 61, 17);
		lblNewLabel_2.setText("保存目录：");
		
		text = new Text(shell, SWT.BORDER);
		text.setEnabled(false);
		text.setEditable(false);
		text.setBounds(99, 127, 237, 23);
		text.setText(   System.getProperty("user.home")+File.separator   );
		
		Button btnNewButton_2 = new Button(shell, SWT.NONE);
		btnNewButton_2.setBounds(276, 171, 80, 27);
		btnNewButton_2.setText("暂   停");
		
		progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(57, 235, 370, 17);
		
		//选择目录
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd = new DirectoryDialog(   shell, SWT.OPEN  );
				//FileDialog fd = new FileDialog(   shell, SWT.OPEN  );
				dd.setFilterPath(   System.getProperty(  "user.home"  )+ File.separator   );
				String dirPath = dd.open();
				if(  dirPath == null || "".equals( dirPath )  ){
					return;
				}
				text.setText( dirPath );
			}
		});
		
		//下载
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					int threadSize=Integer.parseInt(  combo.getText()   );  //使用5个线程下载
					String addr="http://dldir1.qq.com/qqfile/qq/QQ8.7/19113/QQ8.7.exe";
					URL url=new URL( text1.getText().toString() );  //下载文件的url地址
					String saveDir=text.getText();
					
					dlm =new DownLoadManager(   threadSize, url, saveDir, tpm,  new MyNotify() {					
						@Override  // 所以回调时，notifyResult很有可能争抢资源
						public synchronized void notifyResult(Object obj) {
							// obj就是每个线程下载的长度
							long size = Long.parseLong(  obj.toString()  );
							fileSize+= size;
							
							//System.out.println( "已经下载了:"+fileSize  );
							Display.getDefault().asyncExec(   new Runnable() {
								
								@Override
								public void run() {
									int value =(int)( (fileSize/(double)dlm.getFileTotalSize()) * 100 );
									progressBar.setSelection( value );
									if(   value == 100   ){
										MessageBox md=new MessageBox(   shell, SWT.NONE   );
										md.setText("下载成功");
										md.open();
									}
								}
							});
						}
					}  );
					dlm.startDownLoad();
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
				
			}
		});

	}
}
