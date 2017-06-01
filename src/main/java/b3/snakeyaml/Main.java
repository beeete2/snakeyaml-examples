package b3.snakeyaml;

import b3.snakeyaml.entities.Server;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Main {

    public <T> T loadAs(String text, Class<T> type) {
        Yaml yaml = new Yaml(new LocalDateTimePropertyConstructor());
        return yaml.loadAs(text, type);
    }

    public String dump(Object object) {
        Representer representer = new LocalDateTimeRepresenter();
        representer.addClassTag(Server.class, Tag.MAP);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndicatorIndent(2);
        options.setIndent(4);

        Yaml yaml = new Yaml(representer, options);
        return yaml.dump(object);
    }

    class LocalDateTimeRepresenter extends Representer {

        public LocalDateTimeRepresenter() {
            multiRepresenters.put(LocalDateTime.class, new RepresentLocalDateTime());
        }

        private class RepresentLocalDateTime extends RepresentDate {
            public Node representData(Object data) {
                LocalDateTime localDateTime = (LocalDateTime) data;

                String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return representScalar(getTag(data.getClass(), Tag.TIMESTAMP), date, null);
            }
        }
    }

    class LocalDateTimePropertyConstructor extends Constructor {
        public LocalDateTimePropertyConstructor() {
            yamlClassConstructors.put(NodeId.scalar, new TimeStampConstruct());
        }

        class TimeStampConstruct extends Constructor.ConstructScalar {
            @Override
            public Object construct(Node node) {
                if (node.getTag().equals(Tag.TIMESTAMP)) {
                    Construct dateConstructor = yamlConstructors.get(Tag.TIMESTAMP);
                    Date date = (Date) dateConstructor.construct(node);
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
                    return localDateTime;
                } else {
                    return super.construct(node);
                }
            }
        }
    }

}
