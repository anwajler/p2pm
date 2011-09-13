for lib in lib/*.jar ; do
  LIBS="${LIBS}:$lib"
done

for lib in dist/*.jar ; do
  LIBS="${LIBS}:$lib"
done

java -Dsun.io.serialization.extendedDebugInfo=true -Dfile.encoding=UTF-8 -cp $LIBS pl.edu.pjwstk.mteam.pubsub.tests.Main $*
