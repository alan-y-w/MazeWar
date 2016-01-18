import java.util.Comparator;


public class PacketComparator implements Comparator<Packet>{

	@Override
	public int compare(Packet o1, Packet o2) {
		if (o1.seqNumber < o2.seqNumber)
        {
            return -1;
        }
		if (o1.seqNumber > o2.seqNumber)
        {
            return 1;
        }
        return 0;
	}

}
