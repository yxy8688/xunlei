package bean9;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import threadpool.ThreadPoolManager;

public class Test {
	private static long fileSize=0;

	public static void main(String[] args) throws IOException {
		ThreadPoolManager tpm=new ThreadPoolManager(5);
		
		int threadSize=5;  //使用5个线程下载
		String addr="http://dldir1.qq.com/qqfile/qq/QQ8.7/19113/QQ8.7.exe";
		URL url=new URL( addr );  //下载文件的url地址
		String saveDir=System.getProperty("user.home")+File.separator;
		
		DownLoadManager dlm=new DownLoadManager(   threadSize, url, saveDir, tpm,  new MyNotify() {
			
			@Override  // 所以回调时，notifyResult很有可能争抢资源
			public synchronized void notifyResult(Object obj) {
				// obj就是每个线程下载的长度
				long size = Long.parseLong(  obj.toString()  );
				fileSize+= size;
				
				System.out.println( "已经下载了:"+fileSize+"字节"  );
			}
		}  );
		dlm.startDownLoad();
	}

}
