
public class Sequencer {
	private long _current_count;
	public Sequencer()
	{
		_current_count = 0;
	}
	
	public long GetSequenceNumber()
	{
		_current_count ++;
		return _current_count;
	}
}
