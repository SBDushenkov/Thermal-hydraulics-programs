package freezer;

/***
 * Wrapper class for input data exceptions 
 * @author sdushenkov
 *
 */
class InputDataException extends Exception 
{ 
	private static final long serialVersionUID = 1L;

	public InputDataException(final String s) 
    { 
        super(s); 
    } 
}