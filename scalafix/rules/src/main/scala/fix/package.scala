import scalafix.v0._
import scala.meta._

package object fix {
  def sym(tree: Tree)(implicit index: SemanticdbIndex): Patch = {
    println(index.symbol(tree))
    Patch.empty
  }

  def normalized(symbols: String*)(implicit index: SemanticdbIndex): SymbolMatcher =
    SymbolMatcher.normalized(symbols.map(Symbol(_)): _*)

  def exact(symbols: String*)(implicit index: SemanticdbIndex): SymbolMatcher =
    SymbolMatcher.exact(symbols.map(Symbol(_)): _*)

  def trailingBrackets(tree: Tree, ctx: RuleCtx): Option[(Token.LeftBracket, Token.RightBracket)] =
    for {
      end <- tree.tokens.lastOption
      open <- ctx.tokenList
        .find(end)(_.is[Token.LeftBracket])
        .map(_.asInstanceOf[Token.LeftBracket])
      close <- ctx.matchingParens.close(open)
    } yield (open, close)

  def trailingParens(tree: Tree, ctx: RuleCtx): Option[(Token.LeftParen, Token.RightParen)] =
    for {
      end   <- tree.tokens.lastOption
      open  <- ctx.tokenList.find(end)(_.is[Token.LeftParen]).map(_.asInstanceOf[Token.LeftParen])
      close <- ctx.matchingParens.close(open)
    } yield (open, close)

  def trailingApply(tree: Tree, ctx: RuleCtx): Option[(Token, Token)] =
    trailingParens(tree, ctx).orElse(trailingBrackets(tree, ctx))

  def startsWithParens(tree: Tree): Boolean =
    tree.tokens.headOption.map(_.is[Token.LeftParen]).getOrElse(false)
}
