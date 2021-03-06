package strmean.distances;

import java.util.ArrayList;
import java.util.Properties;
import strmean.data.EDResult;
import strmean.data.Example;
import strmean.data.OpInfo;
import strmean.data.Operation;
import strmean.main.JMathUtils;

public class EDDamerauLevenshtein extends EditDistance {

    static protected final char cIni = '1', cFin = '8';
    static protected final char _vacio = ' ';
    private final float kIgual = 0.0001f;
    protected static int debug = 0;
    protected ArrayList<Operation> _ao = null;

    public EDDamerauLevenshtein(Properties p) {
        super(p);
    }

    /**
     *
     * @param ex
     * @param ey
     * @param computeStatistics
     * @return
     */
    @Override
    public EDResult dEdition(Example ex, Example ey, boolean computeStatistics) {

        EDResult edR = new EDResult(ex, ey);

        char[] x = ex.sequence, y = ey.sequence;
        float[][] al = new float[x.length + 1][y.length + 1];
        float a, b, c, wc, wa, wb;

        al[0][0] = 0;
        for (int i = 1; i <= x.length; i++) {
            al[i][0] = al[i - 1][0] + _sd.del(x[i - 1]);//Borrado
        }
        for (int j = 1; j <= y.length; j++) {
            al[0][j] = al[0][j - 1] + _sd.ins(y[j - 1]);	//Inserción
        }
        for (int i = 1; i <= x.length; i++) {
            for (int j = 1; j <= y.length; j++) {
                a = al[i - 1][j] + _sd.del(x[i - 1]);	//Borrado
                b = al[i][j - 1] + _sd.ins(y[j - 1]);	//Inserción
                c = al[i - 1][j - 1] + _sd.sus(x[i - 1], y[j - 1]);//Sustitucion
                al[i][j] = JMathUtils.fmin(a, b, c);
            }
        }

        if (computeStatistics) {
            // Calcular estadísticas sobre las operaciones de edición sobre la primera 
            /**
             * JComment: Seria mejor implementar esto de modo que en los
             * ejemplos se guarden las operaciones directamente de modo que no
             * sea preciso referenciar a SymbolDif desde aqui y desde Example
             * (es decir que sea transparente el mecanismo de los indices de los
             * simbolos)
             */
            int x0 = x.length, y0 = y.length;
            int seqOrd = 0;
            while (x0 != 0 || y0 != 0) {
                int actualCharX = x0 - 1;
                int actualCharY = y0 - 1;
                wa = (x0 > 0) ? _sd.del(x[actualCharX]) : Integer.MAX_VALUE;
                a = (x0 > 0) ? al[actualCharX][y0] + wa : Integer.MAX_VALUE;	//Borrado

                wb = (y0 > 0) ? _sd.ins(y[actualCharY]) : Integer.MAX_VALUE;
                b = (y0 > 0) ? al[x0][actualCharY] + wb : Integer.MAX_VALUE;	//Inserción

                wc = (x0 > 0 && y0 > 0) ? _sd.sus(x[actualCharX], y[actualCharY]) : Integer.MAX_VALUE;
                c = (x0 > 0 && y0 > 0) ? al[actualCharX][actualCharY] + wc : Integer.MAX_VALUE;//Sustitucion

                if (c == al[x0][y0]) {  //Sustitución
                    edR.getOperations().add(new Operation('s',
                            x[actualCharX],
                            y[actualCharY],
                            actualCharX,
                            seqOrd,
                            new OpInfo(wc, 0, 0))
                    );

                    seqOrd++;
                    x0--;
                    y0--;
                } else {
                    if (b == al[x0][y0]) {   //Inserción
                        edR.getOperations().add(new Operation('i',
                                y[actualCharY],
                                y[actualCharY],
                                x0,
                                seqOrd,
                                new OpInfo(wb, 0, 0))
                        );
                        seqOrd++;
                        y0--;
                    } else {
                        if (a == al[x0][y0]) {   //borrado
                            edR.getOperations().add(new Operation('d',
                                    x[actualCharX],
                                    x[actualCharX],
                                    actualCharX,
                                    seqOrd,
                                    new OpInfo(wa, 0, 0))
                            );
                            seqOrd++;
                            x0--;
                        } else {
                            System.err.println("dEdicion(Example, Example, boolean): Error calculando la lista de operaciones");
                            System.err.println("e_x: " + ex.toString() + " e_y: " + ey.toString());
                            System.err.println("x0: " + x0 + " y0: " + y0);
                            System.exit(1);
                        }
                    }
                }
            } //while
        }

        edR.dist = al[x.length][y.length];
        return edR;
    }

    public float dEdition(char[] ex, char[] ey, int lx, int ly) {
        char[] x = ex, y = ey;

        float[][] al = new float[lx + 1][ly + 1];
        float a, b, c, res;

        al[0][0] = 0;
        for (int i = 1; i <= lx; i++) {
            al[i][0] = al[i - 1][0] + _sd.del(x[i - 1]);//Borrado
        }
        for (int j = 1; j <= ly; j++) {
            al[0][j] = al[0][j - 1] + _sd.ins(y[j - 1]);	//Inserción
        }
        for (int i = 1; i <= lx; i++) {
            for (int j = 1; j <= ly; j++) {
                a = al[i - 1][j] + _sd.del(x[i - 1]);	//Borrado
                b = al[i][j - 1] + _sd.ins(y[j - 1]);	//Inserción
                c = al[i - 1][j - 1] + _sd.sus(x[i - 1], y[j - 1]);//Sustitucion
                al[i][j] = JMathUtils.fmin(a, b, c);
            }
        }

        res = al[lx][ly];

        return res;
    }
}
