package disjunctive;

public class ATask {
    protected int id;
    protected int est;
    protected int lct;
    protected int p;

    public ATask(int id, int est, int lct, int p) {
        this.id = id;
        this.est = est;
        this.lct = lct;
        this.p = p;
    }

    public int getEst() {
        return est;
    }

    public void setEst(int est) {
        this.est = est;
    }

    public int getLst() {
        return lct - p;
    }

    public int getEct() {
        return est + p;
    }

    public int getLct() {
        return lct;
    }

    public void setLct(int lct) {
        this.lct = lct;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getId() {
        return id - 1;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasCompulsaryPart() {
        return getLst() < getEct();
    }
}
