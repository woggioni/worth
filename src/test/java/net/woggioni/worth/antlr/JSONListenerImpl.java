package net.woggioni.worth.antlr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import net.woggioni.worth.serialization.ValueParser;
import net.woggioni.worth.xface.Value;

public class JSONListenerImpl extends ValueParser implements JSONListener {

    public Value result = null;

    private String unquote(String quoted) {
        return quoted.substring(1, quoted.length() - 1);
    }

    @Override
    public void enterJson(JSONParser.JsonContext ctx) {
    }

    @Override
    public void exitJson(JSONParser.JsonContext ctx) {
        result = stack.getFirst().value;
        stack.clear();
    }

    @Override
    public void enterObj(JSONParser.ObjContext ctx) {
        beginObject();
    }

    @Override
    public void exitObj(JSONParser.ObjContext ctx) {
        endObject();
    }

    @Override
    public void enterPair(JSONParser.PairContext ctx) {
        objectKey(unquote(ctx.STRING().getText()));
    }

    @Override
    public void exitPair(JSONParser.PairContext ctx) {

    }

    @Override
    public void enterArray(JSONParser.ArrayContext ctx) {
        beginArray();
    }

    @Override
    public void exitArray(JSONParser.ArrayContext ctx) {
        endArray();
    }

    @Override
    public void enterValue(JSONParser.ValueContext ctx) {
        if (ctx.obj() != null) {
        } else if (ctx.array() != null) {
        } else if (ctx.STRING() != null) {
            stringValue(unquote(ctx.STRING().getText()));
        } else if (ctx.TRUE() != null) {
            booleanValue(true);
        } else if (ctx.FALSE() != null) {
            booleanValue(false);
        } else if (ctx.NULL() != null) {
            nullValue();
        } else if (ctx.NUMBER() != null) {
            String text = ctx.NUMBER().getText();
            if (text.indexOf('.') < 0)
                integerValue(Long.valueOf(text));
            else
                floatValue(Float.valueOf(text));
        }
    }

    @Override
    public void exitValue(JSONParser.ValueContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode node) {

    }

    @Override
    public void visitErrorNode(ErrorNode node) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {

    }
}
