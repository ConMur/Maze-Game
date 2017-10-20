package networking;

//TODO: CHECK ADDFIRST AND ADDLAST IN RELATION TO c++ PUSH_BACK AND PUSH_FRONT
import java.util.LinkedList;

public class PacketQueue extends LinkedList<PacketData>
{
	private static final long serialVersionUID = 8725974021848406663L;

	public boolean exists(int sequence)
	{
		for (PacketData data : this)
		{
			if (data.sequence == sequence)
				return true;
		}
		return false;
	}

	public void insertSorted(final PacketData packetData, int maxSequence)
	{
		if (this.isEmpty())
		{
			this.addLast(packetData);
		}
		else
		{
			boolean moreRecent = isSequenceMoreRecent(packetData.sequence, this.getFirst().sequence, maxSequence);
			if (!moreRecent)
			{
				this.addFirst(packetData);
			}
			else if (moreRecent)
			{
				this.addLast(packetData);
			}
			else
			{
				int pos = 0;
				for (PacketData data : this)
				{
					if (isSequenceMoreRecent(data.sequence, packetData.sequence, maxSequence))
					{
						this.add(pos, packetData);
					}
					pos++;
				}
			}
		}
	}

	//TODO: complete copying this
	public void verifyIsSorted(int maxSequence)
	{
		
	}

	private boolean isSequenceMoreRecent(int s1, int s2, int maxSequence)
	{
		return (s1 > s2) && (s1 - s2 <= maxSequence / 2) || (s2 > s1) && (s2 - s1 > maxSequence / 2);
	}
}
