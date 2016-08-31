package entrants.pacman.dalhousie;//package pacman.controllers.osc;

/**
 * Created by happywu on 16-8-2.
 */
public class Triple<A, B, C> {
    public final A fst;
    public final B snd;
    public final C trd;

    public Triple(A fst, B snd, C trd) {
        this.fst = fst;
        this.snd = snd;
        this.trd = trd;
    }

    public String toString() {
        return "Triple[" + fst + "," + snd + "," + trd + "]";
    }

    private static boolean equals(Object x, Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    public boolean equals(Object other) {
        return
            other instanceof Triple<?,?,?> &&
            equals(fst, ((Triple<?,?,?>)other).fst) &&
            equals(snd, ((Triple<?,?,?>)other).snd) &&
            equals(trd, ((Triple<?,?,?>)other).trd);
    }

    public int hashCode() {
        if (fst == null) return (snd == null) ? 0 : snd.hashCode() + 1;
        else if (snd == null) return fst.hashCode() + 2;
        else return fst.hashCode() * 17 + snd.hashCode();
    }

    public static <A,B,C> Triple<A,B,C> of(A a, B b, C c) {
        return new Triple<A,B,C>(a,b,c);
    }
}
