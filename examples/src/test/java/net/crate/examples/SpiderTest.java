package net.crate.examples;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.crate.examples.Spider.Ostrich;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SpiderTest {

  @Test
  public void test() throws Exception {
    Date utilDate = new Date();
    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
    Spider<Date, java.sql.Date> spider = Spider.builder()
        .eyes(utilDate)
        .hair(utilDate)
        .legs(utilDate)
        .teeth(sqlDate)
        .feet(utilDate)
        .hands(utilDate);
    Ostrich<Date, java.sql.Date> ostrich = Ostrich.builder()
        .spider(spider)
        .legs(new HashMap<>())
        .wings(new HashMap<>())
        .feet(new HashMap<>())
        .toes(new HashMap<>());
    assertThat(spider.eyes(), is(utilDate));
    assertThat(spider.hair(), is(Optional.of(utilDate)));
    assertThat(spider.legs(), is(utilDate));
    assertThat(spider.teeth(), is(sqlDate));
    assertThat(spider.feet(), is(utilDate));
    assertThat(spider.hands(), is(utilDate));
    assertThat(ostrich.spider(), is(Optional.of(spider)));
    assertThat(ostrich.legs().size(), is(0));
    assertThat(ostrich.wings().size(), is(0));
    assertThat(ostrich.feet().map(Map::size).orElse(-1), is(0));
    assertThat(ostrich.toes().size(), is(0));
  }

  @Test
  public void testSteps() {
    Date utilDate = new Date();
    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
    Spider_Crate.Eyes<Date> eyes = Spider.builder()
        .eyes(utilDate);
    assertThat(eyes.eyes, is(utilDate));
    Spider_Crate.Hair<Date> hair = eyes.hair(utilDate);
    assertThat(hair.hair, is(Optional.of(utilDate)));
    assertThat(hair.up.eyes, is(utilDate));
    Spider_Crate.Legs<Date> legs = hair.legs(utilDate);
    assertThat(legs.legs, is(utilDate));
    assertThat(legs.up.hair, is(Optional.of(utilDate)));
    assertThat(legs.up.up.eyes, is(utilDate));
    Spider_Crate.Teeth<Date, java.sql.Date> teeth = legs.teeth(sqlDate);
    assertThat(teeth.teeth, is(sqlDate));
    assertThat(teeth.up.legs, is(utilDate));
    assertThat(teeth.up.up.hair, is(Optional.of(utilDate)));
    assertThat(teeth.up.up.up.eyes, is(utilDate));
  }
}