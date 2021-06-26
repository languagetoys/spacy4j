package edu.guym.spacyj.api.containers;

import edu.guym.spacyj.api.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A Doc is a sequence of {@link Token}.
 * The Doc object holds a list of {@link TokenData} objects. {@link Span} and {@link Token} are views of this array.
 * <p>
 * The API is greatly inspired by <a href="https://spacy.io/api/doc">Spacy</a>, but exposes less functionality.
 * Even the docs are mostly copy-pasted from the official Spacy docs.
 */
public final class Doc {

    private final String text;
    private final List<TokenData> tokenData;

    private Doc(String text, List<TokenData> tokenData) {
        this.text = text;
        this.tokenData = Objects.requireNonNull(tokenData);
    }

    public static Doc create(String text, List<TokenData> tokens) {
        return new Doc(text, tokens);
    }

    public static Doc create(List<TokenData> tokens) {
        return new Doc(TextUtils.writeTextWithoutWs(tokens), tokens);
    }

    /**
     * A string representation of the document text.
     */
    public final String text() {
        return text;
    }

    /**
     * Returns true if the doc contains no tokens.
     */
    public final boolean isEmpty() {
        return tokenData.isEmpty();
    }

    /**
     * The character offset for the start of the document.
     */
    public final int startChar() {
        return isEmpty() ? 0 : tokenData.get(0).beginOffset();
    }

    /**
     * The character offset for the end of the document.
     */
    public final int endChar() {
        return isEmpty() ? 0 : tokenData.get(size() - 1).beginOffset();
    }

    /**
     * Get all spans representing the sentences in document.
     */
    public final List<Span> sentences() {
        int[] indexes =
                Stream.of(
                        IntStream.range(0, tokenData.size())
                                .filter(i -> tokenData.get(i).isSentenceStart()),
                        IntStream.of(tokenData.size())
                ).flatMapToInt(s -> s).toArray();

        return IntStream
                .range(0, indexes.length - 1)
                .mapToObj(i -> tokenData.subList(indexes[i], indexes[i + 1]))
                .filter(l -> !l.isEmpty())
                .map(sent -> {
                    int start = sent.get(0).index();
                    int end = sent.get(sent.size() - 1).index() + 1;
                    return spanOf(start, end);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all tokens in doc.
     */
    public final List<Token> tokens() {
        return tokenData.stream()
                .map(t -> Token.create(this, t.index()))
                .collect(Collectors.toList());
    }

    /**
     * Get all tokens in doc as a stream.
     */
    public final Stream<Token> stream() {
        return tokens().stream();
    }

    /**
     * Get a token at position i.
     *
     * @param i the index of the desired token.
     * @throws IndexOutOfBoundsException if i is out of bounds
     */
    public final Token getToken(int i) throws IndexOutOfBoundsException {
        Objects.checkIndex(i, size());
        return tokens().get(i);
    }

    /**
     * Get the number of tokens in the document.
     */
    public final int size() {
        return tokenData.size();
    }

    /**
     * Get a span of [start, end). Indices pertain to document token offsets.
     * Similar to sublist of List.
     *
     * @param start offset of the first token to include in the span
     * @param end   offset of the last token in the span (exclusive)
     */
    public final Span spanOf(int start, int end) {
        Objects.checkFromToIndex(start, end, tokenData.size());
        return Span.create(this, start, end);
    }

    /**
     * Get the underlying token data for serialization/deserialization and creation of {@link Doc} objects.
     */
    public final List<TokenData> tokenData() {
        return tokenData;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doc doc = (Doc) o;
        return text.equals(doc.text) && tokenData.equals(doc.tokenData);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(text, tokenData);
    }

    @Override
    public final String toString() {
        return "Doc{" +
                "text='" + text + '\'' +
                ", tokenData=" + tokenData +
                '}';
    }

}
