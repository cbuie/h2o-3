package water.rapids.ast.prims.mungers;

import water.fvec.Frame;
import water.fvec.Vec;
import water.rapids.Env;
import water.rapids.ast.Ast;
import water.rapids.vals.ValNums;
import water.rapids.ast.AstFunction;

import java.util.ArrayList;

/**
 */
public class AstFilterNaCols extends AstFunction {
  @Override
  public String[] args() {
    return new String[]{"ary", "fraction"};
  }

  /* (filterNACols frame frac) */
  @Override
  public int nargs() {
    return 1 + 2;
  }

  @Override
  public String str() {
    return "filterNACols";
  }

  @Override
  public ValNums apply(Env env, Env.StackHelp stk, Ast asts[]) {
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    double frac = asts[2].exec(env).getNum();
    double nrow = fr.numRows() * frac;
    Vec vecs[] = fr.vecs();
    ArrayList<Double> idxs = new ArrayList<>();
    for (double i = 0; i < fr.numCols(); i++)
      if (vecs[(int) i].naCnt() < nrow)
        idxs.add(i);
    double[] include_cols = new double[idxs.size()];
    int i = 0;
    for (double d : idxs)
      include_cols[i++] = d;
    return new ValNums(include_cols);
  }
}
