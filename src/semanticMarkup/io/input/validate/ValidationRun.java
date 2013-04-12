package semanticMarkup.io.input.validate;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.log.LogLevel;

public class ValidationRun implements Runnable {

	private boolean result = false;
	private IVolumeValidator volumeValidator;
	private File file;
	private Set<IValidationRunListener> validationRunListeners = new HashSet<IValidationRunListener>();
	
	public ValidationRun(IVolumeValidator volumeValidator, File file) {
		this.volumeValidator = volumeValidator;
		this.file = file;
	}
	
	@Override
	public void run() {
		log(LogLevel.DEBUG, "Start validating using " + volumeValidator.getClass());
		this.result = volumeValidator.validate(file);
		log(LogLevel.DEBUG, "Done validating using " + volumeValidator.getClass());
		this.notifyValidationRunListeners();
	}
	
	public boolean getResult() {
		return result;
	}

	public void addValidationRunListener(IValidationRunListener validationRunListener) {
		this.validationRunListeners.add(validationRunListener);
	}
	
	public void removeValidationRunListener(IValidationRunListener validationRunListener) {
		this.validationRunListeners.remove(validationRunListener);
	}
	
	private void notifyValidationRunListeners() {
		for(IValidationRunListener validationRunListener : this.validationRunListeners) {
			validationRunListener.validationDone(this.result, volumeValidator);
		}
	}

}
