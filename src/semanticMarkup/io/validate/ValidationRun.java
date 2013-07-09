package semanticMarkup.io.validate;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import semanticMarkup.log.LogLevel;

/**
 * ValidationRun can be passed to a thread to run an IVolumeValidator
 * @author rodenhausen
 */
public class ValidationRun implements Callable<Boolean> {

	private boolean result = false;
	private IVolumeValidator volumeValidator;
	private File file;
	private CountDownLatch latch;
	
	/**
	 * @param volumeValidator
	 * @param file
	 */
	public ValidationRun(IVolumeValidator volumeValidator, File file) {
		this.volumeValidator = volumeValidator;
		this.file = file;
	}

	@Override
	public Boolean call() throws Exception {
		log(LogLevel.DEBUG, "Start validating using " + volumeValidator.getClass());
		this.result = volumeValidator.validate(file);
		log(LogLevel.DEBUG, "Done validating using " + volumeValidator.getClass());
		latch.countDown();
		return result;
	}


	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
}
