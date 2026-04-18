package profile;

public class Timepoint {
    //Attributes used to modelize the linked list
    public Timepoint next;
    public Timepoint previous;

    //Attributes of a Timepoint
    public int time;
    public int capacity;
    public boolean isBound;
    public boolean isNew;

    //Attributes used by the Edge-Finder filtering algorithm
    public int increment;
    public int incrementMax;
    public int hMaxTotal;
    public int overflow;
    public int cons;
    public int energy;

    public Timepoint(int ptime, int pcapacity)
    {
        next = null;
        previous = null;
        time = ptime;
        capacity = pcapacity;
        increment = 0;
        incrementMax = 0;
        overflow = 0;
        cons = 0;
        isBound = false;
        isNew = false;
        energy = 0;
    }

    public void InsertAfter(Timepoint tp)
    {
        tp.previous = this;
        tp.next = this.next;
        if(next != null)
        {
            next.previous = tp;
        }
        next = tp;
    }

    public void InsertBefore(Timepoint tp)
    {
        tp.next = this;
        tp.previous = this.previous;
        if(previous != null)
        {
            previous.next = tp;
        }
        previous = tp;
    }

    public void Remove()
    {
        if (this == null)
            return;
        Timepoint previous = this.previous;
        Timepoint next = this.next;
        previous.next = next;
        next.previous = previous;
    }

    public int rev_overflow() {
        return -this.overflow;
    }

    @Override
    public String toString() {
        return "Timepoint : (t = " + this.time + ", next = " + this.next.time + ", c = " + this.capacity + ")";
    }
}
