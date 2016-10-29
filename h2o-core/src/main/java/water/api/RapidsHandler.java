package water.api;

import org.reflections.Reflections;
import water.H2O;
import water.Key;
import water.api.schemas3.*;
import water.api.schemas3.RapidsHelpV3.RapidsExpressionV3;
import water.exceptions.H2OIllegalArgumentException;
import water.rapids.ast.Ast;
import water.rapids.Rapids;
import water.rapids.Session;
import water.rapids.vals.Val;
import water.util.Log;
import water.util.StringUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

public class RapidsHandler extends Handler {

  public RapidsSchemaV3 exec(int version, RapidsSchemaV3 rapids) {
    if (rapids == null) return null;
    if (!StringUtils.isNullOrEmpty(rapids.id))
      throw new H2OIllegalArgumentException("Field RapidsSchemaV3.id is deprecated and should not be set " + rapids.id);
    if (StringUtils.isNullOrEmpty(rapids.ast)) return rapids;
    if (StringUtils.isNullOrEmpty(rapids.session_id))
      rapids.session_id = "_specialSess";

    Session ses = RapidsHandler.SESSIONS.get(rapids.session_id);
    if (ses == null) {
      ses = new Session();
      RapidsHandler.SESSIONS.put(rapids.session_id, ses);
    }

    Val val;
    try {
      // This call is synchronized on the session instance
      val = Rapids.exec(rapids.ast, ses);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Throwable e) {
      Log.err(e);
      e.printStackTrace();
      throw e;
    }

    switch (val.type()) {
      case Val.NUM:  return new RapidsNumberV3(val.getNum());
      case Val.NUMS: return new RapidsNumbersV3(val.getNums());
      case Val.ROW:  return new RapidsNumbersV3(val.getRow());
      case Val.STR:  return new RapidsStringV3(val.getStr());
      case Val.STRS: return new RapidsStringsV3(val.getStrs());
      case Val.FRM:  return new RapidsFrameV3(val.getFrame());
      case Val.FUN:  return new RapidsFunctionV3(val.getFun().toString());
      default:       throw H2O.fail();
    }
  }

  public RapidsHelpV3 genHelp(int version, SchemaV3 noschema) {
    Reflections reflections = new Reflections("water.rapids");
    RapidsHelpV3 res = new RapidsHelpV3();
    res.syntax = processAstClass(Ast.class, reflections);
    return res;
  }

  private RapidsExpressionV3 processAstClass(Class<? extends Ast> clz, Reflections refl) {
    ArrayList<RapidsExpressionV3> subs = new ArrayList<>();
    for (Class<? extends Ast> subclass : refl.getSubTypesOf(clz))
      if (subclass.getSuperclass() == clz)
        subs.add(processAstClass(subclass, refl));

    RapidsExpressionV3 target = new RapidsExpressionV3();
    target.name = clz.getSimpleName();
    target.is_abstract = Modifier.isAbstract(clz.getModifiers());
    if (!target.is_abstract) {
      try {
        Ast m = clz.newInstance();
        target.pattern = m.example();
        target.description = m.description();
      }
      catch (IllegalAccessException e) { throw H2O.fail("A"); }
      catch (InstantiationException e) { throw H2O.fail("B"); }
    }
    target.sub = subs.toArray(new RapidsExpressionV3[subs.size()]);

    return target;
  }


  /** Map of session-id (sent by the client) to the actual session instance. */
  public static HashMap<String, Session> SESSIONS = new HashMap<>();

  @SuppressWarnings("unused") // called through reflection by RequestServer
  public InitIDV3 startSession(int version, InitIDV3 p) {
    p.session_key = "_sid" + Key.make().toString().substring(0,5);
    return p;
  }

  @SuppressWarnings("unused") // called through reflection by RequestServer
  public InitIDV3 endSession(int version, InitIDV3 p) {
    if (SESSIONS.get(p.session_key) != null) {
      try {
        SESSIONS.get(p.session_key).end(null);
        SESSIONS.remove(p.session_key);
      } catch (Throwable ex) {
        throw SESSIONS.get(p.session_key).endQuietly(ex);
      }
    }
    return p;
  }
}
