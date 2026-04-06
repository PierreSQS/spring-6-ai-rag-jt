package guru.springframework.springairag.model;

/**
 * Immutable value object representing the AI-generated answer returned to the caller.
 * Using a Java Record keeps the model concise — the compiler generates the
 * constructor, accessor, equals/hashCode, and toString automatically.
 * <p>
 * Created by Pierrot, 2026-04-06.
 */
public record Answer(String answer) {
}