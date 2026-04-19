package dev.forint.deafmute.modules.recognition.engine;

import java.util.List;

public final class BasicGestureDetector {

    public static final int LANDMARK_POINT_COUNT = 21;

    private static final double DEFAULT_OK_DISTANCE_THRESHOLD = 0.06D;
    private static final double DEFAULT_V_GAP_THRESHOLD = 0.04D;
    private static final List<String> SUPPORTED_GESTURE_CODES = List.of(
            "is_thumbs_up",
            "is_v_sign",
            "is_four_sign",
            "is_fist",
            "is_ok_sign"
    );

    private BasicGestureDetector() {
    }

    public static String detectFirstMatch(List<RecognitionEngineLandmark> landmarks, List<String> gestureOrder) {
        if (!valid(landmarks) || gestureOrder == null || gestureOrder.isEmpty()) {
            return null;
        }

        for (String gestureTag : gestureOrder) {
            if (matches(landmarks, gestureTag)) {
                return gestureTag;
            }
        }
        return null;
    }

    public static List<String> detectCandidates(List<RecognitionEngineLandmark> landmarks, List<String> gestureOrder) {
        if (!valid(landmarks) || gestureOrder == null || gestureOrder.isEmpty()) {
            return List.of();
        }
        return gestureOrder.stream()
                .distinct()
                .filter(gestureTag -> matches(landmarks, gestureTag))
                .toList();
    }

    public static List<String> supportedGestureCodes() {
        return SUPPORTED_GESTURE_CODES;
    }

    public static boolean matches(List<RecognitionEngineLandmark> landmarks, String gestureTag) {
        if (!valid(landmarks) || gestureTag == null) {
            return false;
        }

        return switch (gestureTag) {
            case "is_thumbs_up" -> isThumbsUp(landmarks);
            case "is_v_sign" -> isVSign(landmarks);
            case "is_four_sign" -> isFourSign(landmarks);
            case "is_fist" -> isFist(landmarks);
            case "is_ok_sign" -> isOkSign(landmarks);
            default -> false;
        };
    }

    private static boolean isThumbsUp(List<RecognitionEngineLandmark> lm) {
        boolean indexDown = fingerDown(lm, 8, 6);
        boolean middleDown = fingerDown(lm, 12, 10);
        boolean ringDown = fingerDown(lm, 16, 14);
        boolean pinkyDown = fingerDown(lm, 20, 18);
        boolean thumbUp = lm.get(4).getY() < lm.get(3).getY();
        return thumbUp && indexDown && middleDown && ringDown && pinkyDown;
    }

    private static boolean isVSign(List<RecognitionEngineLandmark> lm) {
        boolean indexUp = fingerUp(lm, 8, 6);
        boolean middleUp = fingerUp(lm, 12, 10);
        boolean ringDown = fingerDown(lm, 16, 14);
        boolean pinkyDown = fingerDown(lm, 20, 18);
        boolean fingerGap = Math.abs(lm.get(8).getX() - lm.get(12).getX()) > DEFAULT_V_GAP_THRESHOLD;
        return indexUp && middleUp && ringDown && pinkyDown && fingerGap;
    }

    private static boolean isFourSign(List<RecognitionEngineLandmark> lm) {
        boolean indexUp = fingerUp(lm, 8, 6);
        boolean middleUp = fingerUp(lm, 12, 10);
        boolean ringUp = fingerUp(lm, 16, 14);
        boolean pinkyUp = fingerUp(lm, 20, 18);
        boolean thumbDown = lm.get(4).getX() < lm.get(3).getX() || lm.get(4).getY() > lm.get(3).getY();
        return indexUp && middleUp && ringUp && pinkyUp && thumbDown;
    }

    private static boolean isFist(List<RecognitionEngineLandmark> lm) {
        int fingersDown = 0;
        for (int[] pair : new int[][]{{8, 6}, {12, 10}, {16, 14}, {20, 18}}) {
            if (fingerDown(lm, pair[0], pair[1])) {
                fingersDown++;
            }
        }
        return fingersDown >= 4;
    }

    private static boolean isOkSign(List<RecognitionEngineLandmark> lm) {
        boolean thumbIndexClose = distance(lm.get(4), lm.get(8)) < DEFAULT_OK_DISTANCE_THRESHOLD;
        boolean middleUp = fingerUp(lm, 12, 10);
        boolean ringUp = fingerUp(lm, 16, 14);
        boolean pinkyUp = fingerUp(lm, 20, 18);
        return thumbIndexClose && middleUp && ringUp && pinkyUp;
    }

    private static boolean fingerUp(List<RecognitionEngineLandmark> landmarks, int tipId, int pipId) {
        return landmarks.get(tipId).getY() < landmarks.get(pipId).getY();
    }

    private static boolean fingerDown(List<RecognitionEngineLandmark> landmarks, int tipId, int pipId) {
        return landmarks.get(tipId).getY() > landmarks.get(pipId).getY();
    }

    private static double distance(RecognitionEngineLandmark a, RecognitionEngineLandmark b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static boolean valid(List<RecognitionEngineLandmark> landmarks) {
        return landmarks != null && landmarks.size() == LANDMARK_POINT_COUNT;
    }
}
