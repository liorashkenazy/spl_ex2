package bgu.spl.mics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	private T result;
	private boolean isResolved;

	AtomicReference<T> atomicResult;

	/**
	 * This should be the only public constructor in this class.
	 */
	public Future() {
		//TODO: implement this
		result = null;
		isResolved = false;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
	 * @INV: get() != null
     * @POST: if(@PRE get(0,SECONDS) == null)
	 * 			@POST get(0,SECONDS) != null
	 * @POST: if(@PRE get(0,SECONDS) != null)
	 * 			@PRE get(0,SECONDS) == @POST get(0,SECONDS)
     */
	public T get() {
		//TODO: implement this.
		//option 1
//		if (result != null) {
//			return result;
//		}
//		synchronized (this) {
//			try {
//				wait();
//			} catch (InterruptedException e) {}
//		}
//		return result;
		// option 2
		synchronized (this) {
			while (!isResolved) {
					try {
						wait();
					} catch (InterruptedException m) {}
			}
		}
		return result;
	}
	
	/**
     * Resolves the result of this Future object.
	 * @POST: get(0,SECONDS) == result
	 * @POST: isDone() == true
     */
	public synchronized void resolve (T result) {
		//TODO: implement this.
		this.result = result;
		isResolved = true;
		notifyAll();


//		T currResult = (T) this.atomicResult;
//		while (! atomicResult.compareAndSet(currResult,result)) {
//			currResult = this.result;
//		}
//		isResolved = true;
//		notifyAll();
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
	 * @INV: if(get(0,SECONDS) != null)
	 * 			 isDone() == true
	 * @INV: if(get(0,SECONDS) == null)
	 *  	     isDone() == false
     */
	public boolean isDone() {
		//TODO: implement this.
		return isResolved;

	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
	 * @POST: if (isDone())
	 * 			get(timeout, unit) != null
	 * @POST: if (!isDone())
	 * 			get(timeout, unit) == null
     */
	public T get(long timeout, TimeUnit unit) {
		//TODO: implement this.
		if (isResolved) {
			return result;
		}
//		Thread t1 = Thread.currentThread();
//		Thread t2 = new Thread(() -> {
//			try {
//				Thread.sleep(TimeUnit.MILLISECONDS.convert(timeout,unit));
//			} catch (InterruptedException m) {}
//			t1.interrupted();
//		} );
//		t2.start();

		synchronized (this) {
			try {
				wait(TimeUnit.MILLISECONDS.convert(timeout,unit));
			} catch (InterruptedException m) {}
		}
		return result;
	}

}
