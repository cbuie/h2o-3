package water.rapids.ast.prims.mungers;

import water.fvec.Frame;
import water.rapids.Env;
import water.rapids.ast.Ast;
import water.rapids.vals.ValNum;
import water.rapids.ast.AstFunction;

/**
 */
public class AstNcol extends AstFunction {
  @Override
  public String[] args() {
    return new String[]{"ary"};
  }

  @Override
  public int nargs() {
    return 1 + 1;
  }

  @Override
  public String str() {
    return "ncol";
  }

  @Override
  public ValNum apply(Env env, Env.StackHelp stk, Ast asts[]) {
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    return new ValNum(fr.numCols());
  }
}
