package bean9;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import threadpool.ThreadPoolManager;

/**
 * 下载管理器
 */
public class DownLoadManager {
	private int threadSize; //下载使用的线程数
	private URL url;   //  要下载文件的地址
	private String saveDir;   //保存的目录
	
	private long fileTotalSize; //要下载文件总大小
	private File saveFile;  // 下载后保存的文件
	private long fileSizePerThread;// 每个线程要下载的文件的长度
	private MyNotify myNotify;
	
	private ThreadPoolManager tpm;
	

	public long getFileTotalSize() {
		return this.fileTotalSize;
	}
	
	public DownLoadManager(int threadSize, URL url, String saveDir, ThreadPoolManager tpm, MyNotify myNotify) {
		this.threadSize=threadSize;
		this.url=url;
		this.saveDir=saveDir;
		this.myNotify=myNotify;
		this.tpm=tpm;
	}
	
	public DownLoadManager(int threadSize, URL url, String saveDir, MyNotify myNotify) {
		this.threadSize=threadSize;
		this.url=url;
		this.saveDir=saveDir;
		this.myNotify=myNotify;
	}
	
	public DownLoadManager( int threadSize, URL url, String saveDir ) {
		this.threadSize=threadSize;
		this.url=url;
		this.saveDir=saveDir;
	}
	
	//下载方法
	public void startDownLoad(){
		//取得要保存的新文件的名字
		String newFileName=getNewFileName(  url  );
		try {
			//取得要下载的文件的总大小
			fileTotalSize = getDownLoadFileSize( url );
			//保存的位置
			saveFile =getSaveFile(  null, newFileName  );
			//创建空文件
			createEmptyFile(  saveFile, fileTotalSize );
			//运算每个线程要下载的文件长度
			countFileSizePerThread();
			//开始启动线程,以完成下载
			for(  int i=0; i<this.threadSize; i++  ){
				//DownLoadTask是一个下载的任务，它实现Runnable
				//Thread t = new Thread(  new DownLoadTask(   url, saveFile, fileSizePerThread, i, myNotify   )  );
				//t.start();
				
				if(  tpm != null  ){
					tpm.process(  new DownLoadTask(   url, saveFile, fileSizePerThread, i, myNotify   ) );
				}else{
					Thread t = new Thread(  new DownLoadTask(   url, saveFile, fileSizePerThread, i, myNotify   )  );
					t.start();
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 计算每个线程要下载的文件长度
	 */
	public void countFileSizePerThread(){
		this.fileSizePerThread = this.fileTotalSize%threadSize==0?this.fileTotalSize/threadSize  : this.fileTotalSize/threadSize+1;
	}

	/**
	 * 生成新文件的文件名：yyyyMMddHHmmss.后缀名
	 * @param url
	 * @return
	 */
	private String getNewFileName(  URL url  ) {
		//取出文件的后缀名
		String suffixName=url.getFile().substring( url.getFile().lastIndexOf("."));
		//生成文件的文件名
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName=sdf.format(   new Date() );
		return fileName+suffixName;
	}
	
	private File getSaveFile( String dirName, String fileName ) {
		if( dirName == null ){
			dirName=System.getProperty("user.home")+File.separator;
		}
		File f = new File(  dirName, fileName  );
		return f;
	}
	
	/**
	 * 根据创建的文件位置和文件大小，创建一个固定大小的空间
	 * @param file
	 * @param fileSize
	 * @throws IOException 
	 */
	private void createEmptyFile( File file, long fileLength ) throws IOException {
		RandomAccessFile raf=new RandomAccessFile( file, "rw" ); // 随机访问文件   ->  1.读，写    
																								  //					          2.按指定大小创建空文件setLength()  
																								  //							  3.指定位置
		raf.setLength( fileLength );
		raf.close();
	}

	private long getDownLoadFileSize(URL url) throws IOException {
		HttpURLConnection con=(HttpURLConnection) url.openConnection();
		//关键点,设置请求的方式为HEAD,而不是GET/POST
		con.setRequestMethod("HEAD");//HEAD告诉服务器只要返回访问的文件的信息即可，不要有文件的内容
		con.connect();
		long fileLength=con.getContentLength();
		return fileLength;
	}

}
