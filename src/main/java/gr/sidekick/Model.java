package gr.sidekick;

import java.util.*;
import java.time.*;

public final class Model {
    public static final String OFF = "off";
    public static String id() { return UUID.randomUUID().toString(); }
    public static class Data {
        public int version = 1;
        public List<Team> teams = new ArrayList<>();
        public Map<String,String> leaves = new LinkedHashMap<>();
        public Data() {
            leaves.put(OFF,"Ρεπό");
            for (String name : List.of("Κανονική άδεια","Άδεια ιατρού","Άδεια ασθενείας","Γονική άδεια","Φοιτητική άδεια","Άνευ αποδοχών")) leaves.put(id(),name);
        }
    }
    public static class Team {
        public String id = id(), name;
        public List<Post> posts = new ArrayList<>();
        public List<Employee> employees = new ArrayList<>();
        public Set<String> allowedLeaves = new LinkedHashSet<>();
        public Map<String,Month> months = new TreeMap<>();
        public int maxConsecutive = 5, minRestHours = 11, weeklyOff = 2;
        public boolean preferPaired = true;
        public Team(String name) { this.name = name; allowedLeaves.add(OFF); }
        public Month month(YearMonth ym) { return months.computeIfAbsent(ym.toString(), k -> new Month()); }
        public Post post(String id) { return posts.stream().filter(p -> p.id.equals(id)).findFirst().orElse(null); }
        public Cell cell(String employee, LocalDate date) {
            Month m = months.get(YearMonth.from(date).toString());
            return m == null ? null : m.cells.get(key(employee,date.getDayOfMonth()));
        }
        public String toString() { return name; }
    }
    public static class Post {
        public String id = id(), name, start, end;
        public int[] demand = {1,1,1,1,1,1,1};
        public Post(String name,String start,String end) { this.name=name;this.start=start;this.end=end; }
        public int required(LocalDate day) { return Math.max(1,demand[day.getDayOfWeek().getValue()-1]); }
        public LocalDateTime startAt(LocalDate d) { return d.atTime(LocalTime.parse(start)); }
        public LocalDateTime endAt(LocalDate d) {
            LocalTime a=LocalTime.parse(start), b=LocalTime.parse(end);
            return (b.isAfter(a)?d:d.plusDays(1)).atTime(b);
        }
    }
    public static class Employee {
        public String id = id(), name;
        public Set<String> skills = new LinkedHashSet<>();
        public Employee(String name) { this.name=name; }
    }
    public static class Month {
        public Map<String,Cell> cells = new LinkedHashMap<>();
        public Set<Integer> holidays = new TreeSet<>();
    }
    public static class Cell {
        public String value;
        public boolean locked;
        public Cell(String value,boolean locked) { this.value=value;this.locked=locked; }
    }
    public static String key(String employee,int day) { return employee+":"+day; }
    public static String label(Data data,Team team,String value) {
        if (value==null) return "—";
        Post p=team.post(value);return p==null?data.leaves.getOrDefault(value,"Άγνωστο"):p.name;
    }
}
