package entrants.pacman.dalhousie;//package pacman.controllers.osc;
/**
 * Created by happywu on 16-8-2.
 */
public class BFSnode<A, B, C, D, E> {
    /*
    public final A fst;
    public final B snd;
    public final C trd;
    public final D fth;*/

    public A fst;
    public B snd;
    public C trd;
    public D fth;
    public E sth;

    public BFSnode(A fst, B snd, C trd, D fth, E sth) {
        this.fst = fst;
        this.snd = snd;
        this.trd = trd;
        this.fth = fth;
        this.sth = sth;
    }

    public String toString() {
        return "BFSnode[" + fst + "," + snd + "," + trd + "," + fth + "," + sth + "]";
    }

    private static boolean equals(Object x, Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    public boolean equals(Object other) {
        return
                other instanceof BFSnode<?,?,?,?,?> &&
                        equals(fst, ((BFSnode<?,?,?,?,?>)other).fst) &&
                        equals(snd, ((BFSnode<?,?,?,?,?>)other).snd) &&
                        equals(trd, ((BFSnode<?,?,?,?,?>)other).trd) &&
                        equals(fth, ((BFSnode<?,?,?,?,?>)other).fth) &&
                        equals(sth, ((BFSnode<?,?,?,?,?>)other).sth);
    }


    public static <A,B,C,D,E> BFSnode<A,B,C,D,E> of(A a, B b, C c, D d, E e) {
        return new BFSnode<A,B,C,D,E>(a,b,c,d,e);
    }

}
