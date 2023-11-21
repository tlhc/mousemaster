package jmouseable.jmouseable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record ComboSequence(List<ComboMove> moves) {
    static ComboSequence parseSequence(String movesString,
                                       ComboMoveDuration defaultMoveDuration) {
        String[] moveStrings = movesString.split("\\s+");
        List<ComboMove> moves = new ArrayList<>();
        for (String moveString : moveStrings) {
            Matcher matcher = Pattern.compile("([+\\-#])([a-z]+)((\\d+)-(\\d+))?")
                                     .matcher(moveString);
            if (!matcher.matches())
                throw new IllegalArgumentException("Invalid move: " + moveString);
            boolean press = !moveString.startsWith("-");
            ComboMoveDuration moveDuration;
            if (matcher.group(3) == null)
                moveDuration = defaultMoveDuration;
            else
                moveDuration = new ComboMoveDuration(
                        Duration.ofMillis(Integer.parseUnsignedInt(matcher.group(4))),
                        Duration.ofMillis(Integer.parseUnsignedInt(matcher.group(5))));
            String keyName = matcher.group(2);
            Key key = parseKey(keyName);
            ComboMove move;
            if (press) {
                boolean eventMustBeEaten = moveString.startsWith("+");
                move = new ComboMove.PressComboMove(key, eventMustBeEaten, moveDuration);
            }
            else
                move = new ComboMove.ReleaseComboMove(key, moveDuration);
            moves.add(move);
        }
        return new ComboSequence(List.copyOf(moves));
    }

    public static Key parseKey(String keyName) {
        Key key = Key.keyByName.get(keyName);
        if (key == null)
            throw new IllegalArgumentException("Invalid key: " + keyName);
        return key;
    }

    @Override
    public String toString() {
        return moves.stream().map(Object::toString).collect(Collectors.joining(" "));
    }

}
