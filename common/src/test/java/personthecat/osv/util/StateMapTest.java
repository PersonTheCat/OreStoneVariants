package personthecat.osv.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class StateMapTest {

    @Test
    public void without_removesVariant() {
        final StateMap<Integer> denseLit = new StateMap<>();
        denseLit.put("lit=false", 1);
        denseLit.put("lit=true", 3);

        final StateMap<Integer> denseOnly = new StateMap<>();
        denseOnly.put("", 1);

        assertEquals(denseLit, getDenseLitMap().without("dense", "false"));
        assertEquals(denseOnly, getDenseMap().without("dense", "false"));
    }

    @Test
    public void with_createsPairedMap() {
        final StateMap<Pair<Integer, Integer>> denseLit = new StateMap<>();
        denseLit.put("dense=false,lit=false", Pair.of(1, 1));
        denseLit.put("dense=true,lit=false", Pair.of(2, 1));
        denseLit.put("dense=false,lit=true", Pair.of(1, 2));
        denseLit.put("dense=true,lit=true", Pair.of(2, 2));

        assertEquals(denseLit, getDenseMap().with(getLitMap()));
    }

    @Test
    public void getValue_returnsValue() {
        assertEquals("v3", StateMap.getValue("k1=v1,k2=v2,k3=v3", "k3"));
        assertEquals("true", StateMap.getValue("dense=true,lit=false", "dense"));
        assertNull(StateMap.getValue("one=1,two=2,three=3", "four"));
    }

    @Test
    public void contains_matchesValue() {
        assertTrue(StateMap.contains("k1=v1,k2=v2,k3=v3", "k3=v3"));
        assertTrue(StateMap.contains("dense=true,lit=false", "dense=true"));
        assertFalse(StateMap.contains("one=1,two=2,three=3", "four=4"));
        assertFalse(StateMap.contains("k1=v1,k2=v2,k3=v3", "k3=false"));
    }

    @Test
    public void containsKey_matchesKey() {
        assertTrue(StateMap.containsKey("k1=v1,k2=v2,k3=v3", "k3"));
        assertTrue(StateMap.containsKey("dense=true,lit=false", "dense"));
        assertFalse(StateMap.containsKey("one=1,two=2,three=3", "four"));
    }

    @Test
    public void is_matchesExactly() {
        assertTrue(StateMap.is("dense=true", "dense", "true"));
        assertTrue(StateMap.is("lit=false", "lit", "false"));
        assertFalse(StateMap.is("oxidation=1", "oxidation", "false"));
    }

    @Test
    public void removeVariant_removesVariant() {
        assertEquals("dense=true", StateMap.removeVariant("dense=true,lit=false", "lit=false"));
        assertEquals("lit=false", StateMap.removeVariant("dense=true,lit=false",  "dense=true"));
    }

    @Test
    public void addVariant_addsVariant() {
        assertEquals("dense=true,lit=false", StateMap.addVariant("dense=true", "lit=false"));
        assertEquals("lit=false", StateMap.addVariant("", "lit=false"));
    }

    @Test
    public void setVariant_setsVariant() {
        assertEquals("dense=false,lit=true", StateMap.setVariant("dense=true,lit=true", "dense=false"));
        assertEquals("dense=true,lit=false", StateMap.setVariant("dense=true,lit=true", "lit=false"));
    }

    private static StateMap<Integer> getDenseMap() {
        final StateMap<Integer> map = new StateMap<>();
        map.put("dense=false", 1);
        map.put("dense=true", 2);
        return map;
    }

    private static StateMap<Integer> getLitMap() {
        final StateMap<Integer> map = new StateMap<>();
        map.put("lit=false", 1);
        map.put("lit=true", 2);
        return map;
    }

    private static StateMap<Integer> getDenseLitMap() {
        final StateMap<Integer> map = new StateMap<>();
        map.put("dense=false,lit=false", 1);
        map.put("dense=true,lit=false", 2);
        map.put("dense=false,lit=true", 3);
        map.put("dense=true,lit=true", 4);
        return map;
    }
}
