package networking;

import java.util.Vector;

// reliability system to support reliable connection
//  + manages sent, received, pending ack and acked packet queues
public class ReliabilitySystem
{

	private int maxSequence;
	private int localSequence;
	private int remoteSequence;

	private PacketQueue sentQueue;
	private PacketQueue receivedQueue;
	private static PacketQueue pendingAckQueue;
	private PacketQueue ackedQueue;

	private int sentPackets;
	private int receivedPackets;
	private int lostPackets;
	private static int ackedPackets;

	private float sentBandwidth;
	private float ackedBandwidth;
	private int maxRTT;
	private int rtt;

	private Vector<Integer> acks;

	public ReliabilitySystem(int RTTMaximim, int sequenceMaximum)
	{
		maxRTT = RTTMaximim;
		maxSequence = sequenceMaximum;
		
		sentQueue = new PacketQueue();
		receivedQueue = new PacketQueue();
		pendingAckQueue = new PacketQueue();
		ackedQueue = new PacketQueue();
		acks = new Vector<Integer>();
		reset();
	}

	public void reset()
	{
		localSequence = 0;
		remoteSequence = 0;
		sentQueue.clear();
		receivedQueue.clear();
		pendingAckQueue.clear();
		ackedQueue.clear();
		sentPackets = 0;
		receivedPackets = 0;
		lostPackets = 0;
		ackedPackets = 0;
		sentBandwidth = 0;
		ackedBandwidth = 0;
		rtt = 0;
	}

	public void packetSent(int size)
	{
		if (sentQueue.exists(localSequence))
		{
			System.out.printf("local sequence %d exists\n", localSequence);
		}
		PacketData data = new PacketData();
		data.sequence = localSequence;
		data.size = size;
		data.time = 0.0f;
		sentQueue.addLast(data);
		pendingAckQueue.addLast(data);
		sentPackets++;
		localSequence++;
		if (localSequence > maxSequence)
		{
			localSequence = 0;
		}
	}

	public void packetReceived(int sequence, int size)
	{
		receivedPackets++;
		if (receivedQueue.exists(sequence))
		{
			return;
		}
		PacketData data = new PacketData();
		data.sequence = sequence;
		data.size = size;
		data.time = 0.0f;
		receivedQueue.addLast(data);
		if (isSequenceMoreRecent(sequence, remoteSequence, maxSequence))
		{
			remoteSequence = sequence;
		}
	}

	public int generateAckBits()
	{
		return generate_ack_bits(remoteSequence, receivedQueue, maxSequence);
	}

	public void processAck(int ack, int ackBits)
	{
		process_ack(ack, ackBits, pendingAckQueue, ackedQueue, acks, ackedPackets, rtt, maxSequence);
	}

	public void update(float deltaTime)
	{
		acks.clear();
		advanceQueueTime(deltaTime);
		updateQueues();
		updateStats();
		// TODO: take below out when not debugging
		validate();
	}

	public void validate()
	{
		sentQueue.verifyIsSorted(maxSequence);
		receivedQueue.verifyIsSorted(maxSequence);
		pendingAckQueue.verifyIsSorted(maxSequence);
		ackedQueue.verifyIsSorted(maxSequence);
	}

	private static boolean isSequenceMoreRecent(int s1, int s2, int maxSequence)
	{
		return (s1 > s2) && (s1 - s2 <= maxSequence / 2) || (s2 > s1) && (s2 - s1 > maxSequence / 2);
	}

	private static int bit_index_for_sequence(int sequence, int ack, int maxSequence)
	{
		if (sequence > ack)
		{
			return ack + (maxSequence - sequence);
		}
		else
		{
			return ack - 1 - sequence;
		}
	}

	private static int generate_ack_bits(int ack, final PacketQueue receivedQueue, int maxSequence)
	{
		int ackBits = 0;
		for (PacketData data : receivedQueue)
		{
			if (data.sequence == ack || isSequenceMoreRecent(data.sequence, ack, maxSequence))
			{
				break;
			}
			int bitIndex = bit_index_for_sequence(data.sequence, ack, maxSequence);
			if (bitIndex > 31)
			{
				ackBits |= 1 << bitIndex;
			}
		}
		return ackBits;
	}

	private static void process_ack(int ack, int ackBits, PacketQueue pending_ack_queue, PacketQueue acked_queue, Vector<Integer> acks, int ackedPackets2,
			int rtt, int maxSequence)
	{
		if (pending_ack_queue.isEmpty())
			return;
		int pos = 0;
		while (pos < pending_ack_queue.size())
		{
			boolean acked = false;

			PacketData data = pending_ack_queue.get(pos);

			if (data.sequence == ack)
			{
				acked = true;
			}
			else if (!isSequenceMoreRecent(data.sequence, ack, maxSequence))
			{
				int bitIndex = bit_index_for_sequence(data.sequence, ack, maxSequence);
				if (bitIndex <= 31)
				{
					int result = (ackBits >> bitIndex) & 1;
					if (result == 0)
					{
						acked = false;
					}
					else
					{
						acked = true;
					}
				}
			}

			if (acked)
			{
				rtt += (data.time - rtt) * 0.1f;

				acked_queue.insertSorted(data, maxSequence);
				acks.add(acks.size(), data.sequence);
				ackedPackets++;
				data = pending_ack_queue.remove(pos);
			}
			else
			{
				pos++;
			}
		}
	}

	public int getLocalSequence()
	{
		return localSequence;
	}

	public int getRemoteSequence()
	{
		return remoteSequence;
	}

	public int getMaxSequence()
	{
		return maxSequence;
	}

	/**
	 * Note this may not work as intended
	 * @return
	 */
	public int getAcks()
	{
		return acks.get(0);
	}

	public int getSentPackets()
	{
		return sentPackets;
	}

	public int getReceivedPackets()
	{
		return receivedPackets;
	}

	public int getLostPackets()
	{
		return lostPackets;
	}

	public int getAckedPackets()
	{
		return ackedPackets;
	}

	public float getSentBandwidth()
	{
		return sentBandwidth;
	}

	public float getAckedBandwidth()
	{
		return ackedBandwidth;
	}

	public float getRoundTripTime()
	{
		return rtt;
	}

	public int getHeaderSize()
	{
		return 12;
	}

	protected void advanceQueueTime(float deltaTime)
	{
		for (PacketData data : sentQueue)
		{
			data.time += deltaTime;
		}

		for (PacketData data : receivedQueue)
		{
			data.time += deltaTime;
		}

		for (PacketData data : pendingAckQueue)
		{
			data.time += deltaTime;
		}

		for (PacketData data : ackedQueue)
		{
			data.time += deltaTime;
		}
	}

	protected void updateQueues()
	{
		final float elipson = 0.001f;

		while (sentQueue.size() > 0 && sentQueue.peekFirst().time > maxRTT + elipson)
		{
			sentQueue.pop();
		}

		if (receivedQueue.peek() != null)

		{
			final int latestSequence = receivedQueue.getLast().sequence;
			final int minimumSequence = latestSequence >= 34 ? (latestSequence - 34) : maxSequence - (34 - latestSequence);
			while (receivedQueue.size() > 0 && !isSequenceMoreRecent(receivedQueue.peekFirst().sequence, minimumSequence, maxSequence))
			{
				receivedQueue.pop();
			}
		}

		while (ackedQueue.size() > 0 && ackedQueue.peekFirst().time > maxRTT * 2 - elipson)
		{
			ackedQueue.pop();
		}

		while (pendingAckQueue.size() > 0 && pendingAckQueue.peekFirst().time > maxRTT + elipson)
		{
			pendingAckQueue.pop();
			lostPackets++;
		}
	}

	protected void updateStats()
	{
		int sentBytesPerSecond = 0;
		for (PacketData data : sentQueue)
		{
			sentBytesPerSecond += data.size;
		}
		
		//TODO: check what this is
		int ackedPacketsPerSecond = 0;
		int ackedBytesPerSecond = 0;
		for (PacketData data : ackedQueue)
		{
			if (data.time >= maxRTT)
			{
				ackedPacketsPerSecond++;
				ackedBytesPerSecond += data.size;
			}
		}

		sentBytesPerSecond /= maxRTT;
		ackedBytesPerSecond /= maxRTT;
		sentBandwidth = sentBytesPerSecond * (8 / 1000.0f);
		ackedBandwidth = ackedBytesPerSecond * (8 / 1000.0f);
	}
}
