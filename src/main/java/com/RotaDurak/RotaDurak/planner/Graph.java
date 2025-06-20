package com.RotaDurak.RotaDurak.planner;

import java.util.*;

//Basit yönsüz ağı temsil eden sınıf. Dijkstra algoritması burda yer alıyor.
public class Graph {
    //bir komşu (edge) bilgisini tutar
    public static class Edge {
        public final long toStationId;
        public final double weight;
        public final Long routeId; //null ise yürüyüş ya da transfer kenarı

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
        adj.computeIfAbsent(to,   k -> new ArrayList<>()).add(new Edge(from,weight,routeId));
    }

    /** Dışarıya komşu kenarlarını açmak için */
    public List<Edge> getNeighbors(long stationId) {
        return adj.getOrDefault(stationId, Collections.emptyList());
    }

    private static class Node implements Comparable<Node> {
        final long stationId;
        final double distance;
        Node(long stationId, double distance) {
            this.stationId = stationId;
            this.distance = distance;
        }
        @Override
        public int compareTo(Node o) {
            return Double.compare(this.distance, o.distance);
        }
    }

        /**
         * Dijkstra algoritması: start'tan goal'a en kısa yolu bulur.
         * @return stationId listesi (start → … → goal)
         */
        public List<Long> shortestPath(long start,long goal) {
            //PriorityQueue<(dist, stationId)>
            Map<Long, Double> dist = new HashMap<>();
            Map<Long,Long> prev = new HashMap<>();
            PriorityQueue<Node> pq = new PriorityQueue<>();

            dist.put(start,0.0);
            pq.add(new Node(start, 0.0));
            while (!pq.isEmpty()) {
                Node node = pq.poll();
                double d = node.distance;
                long u = node.stationId;

                if(d > dist.getOrDefault(u, Double.POSITIVE_INFINITY)) continue;
                if(u == goal) break;

                for (Edge e : getNeighbors(u)) {
                    double nd = d + e.weight;
                    if (nd < dist.getOrDefault(e.toStationId, Double.POSITIVE_INFINITY)) {
                        dist.put(e.toStationId, nd);
                        prev.put(e.toStationId, u);
                        pq.add(new Node(e.toStationId, nd));
                    }
                }
            }

        //geri izleme
        List<Long> path = new ArrayList<>();
        Long cur = goal;
        while (cur != null && cur != start) {
            path.add(cur);
            cur = prev.get(cur);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    //İki nokta arasındaki haversine mesafesini km cinsinden döner
    public static double haversineKm(double lat1,double lon1, double lat2,double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        return R * c;
    }
}
