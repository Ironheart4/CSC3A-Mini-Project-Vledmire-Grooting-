package src;

public class TestHashMap {
    public static void main(String[] args) {
        // Test HashMap
        HashMap<String, Integer> map = new HashMap<>();
        map.put("alpha", 1);
        map.put("beta", 2);
        map.put("gamma", 3);
        map.put("alpha", 99); // update

        System.out.println("size=" + map.size());
        System.out.println("get(alpha)=" + map.get("alpha"));
        System.out.println("get(beta)=" + map.get("beta"));
        System.out.println("get(missing)=" + map.get("missing"));

        System.out.print("keys: ");
        for (String k : map.keySet()) System.out.print(k + " ");
        System.out.println();

        System.out.print("values: ");
        for (Integer v : map.values()) System.out.print(v + " ");
        System.out.println();

        map.remove("beta");
        System.out.println("after remove(beta), size=" + map.size());

        // Test ArrayList iterator
        ArrayList<String> list = new ArrayList<>();
        list.add("x"); list.add("y"); list.add("z");
        System.out.print("ArrayList for-each: ");
        for (String s : list) System.out.print(s + " ");
        System.out.println();

        System.out.println("ALL TESTS PASSED");
    }
}
