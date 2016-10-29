package water.rapids.ast.prims.mungers;

import water.H2O;
import water.MRTask;
import water.fvec.Chunk;
import water.fvec.Frame;
import water.fvec.NewChunk;
import water.fvec.Vec;
import water.rapids.Env;
import water.rapids.Val;
import water.rapids.ast.AstRoot;
import water.rapids.vals.ValFrame;
import water.rapids.vals.ValNum;
import water.rapids.ast.AstFunction;

/**
 * Split out in it's own function, instead of Yet Another UniOp, because it
 * needs a "is.NA" check instead of just using the Double.isNaN hack... because
 * it works on UUID and String columns.
 */
public class AstIsNa extends AstFunction {
  @Override
  public String[] args() {
    return new String[]{"ary"};
  }

  @Override
  public String str() {
    return "is.na";
  }

  @Override
  public int nargs() {
    return 1 + 1;
  }

  @Override
  public Val apply(Env env, Env.StackHelp stk, AstRoot asts[]) {
    Val val = stk.track(asts[1].exec(env));
    switch (val.type()) {
      case Val.NUM:
        return new ValNum(op(val.getNum()));
      case Val.FRM:
        Frame fr = val.getFrame();
        return new ValFrame(new MRTask() {
          @Override
          public void map(Chunk cs[], NewChunk ncs[]) {
            for (int col = 0; col < cs.length; col++) {
              Chunk c = cs[col];
              NewChunk nc = ncs[col];
              for (int i = 0; i < c._len; i++)
                nc.addNum(c.isNA(i) ? 1 : 0);
            }
          }
        }.doAll(fr.numCols(), Vec.T_NUM, fr).outputFrame());
      case Val.STR:
        return new ValNum(val.getStr() == null ? 1 : 0);
      default:
        throw H2O.unimpl("is.na unimpl: " + val.getClass());
    }
  }

  double op(double d) {
    return Double.isNaN(d) ? 1 : 0;
  }
}
