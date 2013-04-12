package semanticMarkup.io.input.validate;

/**
 * An IValidationRunListener is notified when a validation is done
 * @author rodenhausen
 */
public interface IValidationRunListener {

	public void validationDone(boolean result, IVolumeValidator volumeValidator);

}
