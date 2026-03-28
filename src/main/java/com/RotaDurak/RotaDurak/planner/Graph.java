package com.RotaDurak.RotaDurak.planner;

import java.util.*;

//Basit yönsüz ağı temsil eden sınıf. Dijkstra algoritması burda yer alıyor.
public class Graph {
    //bir komşu (edge) bilgisini tutar
    public static class Edge {
        public final long toStationId;
        public final double weight;
        public final Long routeId; //null ise yürüyüş

        public Edge(long toStationId, double weight, Long routeId) {
            this.toStationId = toStationId;
            this.weight = weight;
            this.routeId = routeId;
        }
    }

    //her bir istasyonId için komşu listesi
    private final Map<Long, List<Edge>> adj = new HashMap<>();

    public void addEdge(long from, long to, double weight, Long routeId) {
        adj.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to,weight,routeId));
    }

    /** Dışarıya komşu kenarlarını açmak için */
    public List<Edge> getNeighbors(long stationId) {
        return adj.getOrDefault(stationId, Collections.emptyList());
    }

    //State: (stationId, routeId) çifti
    private record State(long stationId, Long routeId) {}

    private static class Node implements Comparable<Node> {
        final State state;
        final double cost;

        Node(State state, double cost) {
            this.state = state;
            this.cost = cost;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.cost, o.cost);
        }
    }

    /**
     * Aktarma cezalı Dijkstra.
     * State = (istasyon, mevcut hat) — hat değişince penalty uygulanır.
     *
     * @param start     başlangıç istasyon ID
     * @param goal      hedef istasyon ID
     * @param costType  maliyet tipi (aktarma cezasını belirler)
     * @return stationId listesi (start → … → goal), bulunamazsa boş liste
     */
    public List<Long> shortestPath(long start,long goal, CostType costType) {
            double penalty = CostWeights.getTransferPenalty(costType);

            //PriorityQueue<(dist, stationId)>
            Map<State, Double> dist = new HashMap<>();
            Map<State, State> prev = new HashMap<>();
            PriorityQueue<Node> pq = new PriorityQueue<>();

            // Başlangıç: hiçbir hatta binmemişiz (routeId = null)
            State startState = new State(start, null);
            dist.put(startState, 0.0);
            pq.add(new Node(startState, 0.0));

            State goalState = null;

            while (!pq.isEmpty()) {
                Node cur = pq.poll();
                double d = cur.cost;
                State u = cur.state;

                if (d > dist.getOrDefault(u, Double.POSITIVE_INFINITY)) continue;

                if (u.stationId() == goal) {
                    goalState = u;
                    break;
                }

                for (Edge e : getNeighbors(u.stationId())) {
                    // Hat değişimi var mı?
                    boolean isTransfer = (u.routeId() != null)
                            && (e.routeId != null)
                            && (!e.routeId.equals(u.routeId()));

                    double nd = d + e.weight + (isTransfer ? penalty : 0.0);
                    State next = new State(e.toStationId, e.routeId);

                    if (nd < dist.getOrDefault(next, Double.POSITIVE_INFINITY)) {
                        dist.put(next, nd);
                        prev.put(next, u);
                        pq.add(new Node(next, nd));
                    }
                }
    }

        if (goalState == null) {
            // goal state bulunamadıysa, en düşük maliyetli goal state'i ara
            goalState = dist.keySet().stream()
                    .filter(s -> s.stationId() == goal)
                    .min(Comparator.comparingDouble(s -> dist.getOrDefault(s, Double.MAX_VALUE)))
                    .orElse(null);
        }

        if (goalState == null) return Collections.emptyList();

        // Geri izleme
        List<Long> path = new ArrayList<>();
        State cur = goalState;
        while (cur != null && cur.stationId() != start) {
            path.add(cur.stationId());
            cur = prev.get(cur);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    // Geriye dönük uyumluluk için (costType olmadan çağrılırsa TIME varsayılan)
    public List<Long> shortestPath(long start, long goal) {
        return shortestPath(start, goal, CostType.TIME);
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
