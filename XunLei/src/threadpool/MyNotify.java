package threadpool;

/**
 * 回调接口
 */
public interface MyNotify {
	/**
	 * 回调方法：当子线程完成操作，向主线程通信
	 * @param obj:是回调参数，用来存返回给主线程的结果
	 */
	public void notifyResult(  Object obj  );
}
