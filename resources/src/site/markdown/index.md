Shared Resources
-----------

# Shared Resources

This module contains shared resources that can be used in all SpeedTools modules,
such as `log4j.xmlz definition files. Maven will pick the resources up for
the other modules automatically from `<shared-resources>/src/main/resources`.

The advantage of shared resources, even for stuff like log4j configuration files,
is that it makes it very easy to maintain consistent configuration between different
modules.

