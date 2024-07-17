package com.janne.routingsystem.model.incoming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse {
    private Hints hints;
    private Info info;
    private List<Path> paths;

    @Data
    public static class Hints {
        private int visitedNodesSum;
        private double visitedNodesAverage;
    }

    @Data
    public static class Info {
        private List<String> copyrights;
        private int took;
        private String roadDataTimeStamp;
    }

    @Data
    public static class Path {
        private double distance;
        private double weight;
        private long time;
        private int transfers;
        private boolean pointsEncoded;
        private double pointsEncodedMultiplier;
        private List<Double> bbox;
        private String points;
        private List<Instruction> instructions;
        private List<Leg> legs;
        private Map<String, Object> details;
        private double ascend;
        private double descend;
        private String snappedWaypoints;

        @Data
        public static class Instruction {
            private double distance;
            private double heading;
            private int sign;
            private List<Integer> interval;
            private String text;
            private long time;
            private String streetName;
            private double lastHeading;
        }

        @Data
        public static class Leg {
            // Assuming the Leg class is empty, as indicated in the JSON sample.
            // Add fields here if necessary.
        }
    }
}