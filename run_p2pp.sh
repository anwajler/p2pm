for lib in lib/*.jar ; do
  LIBS="${LIBS}:$lib"
done

for lib in dist/*.jar ; do
  LIBS="${LIBS}:$lib"
done

java -server -Xmx512m -Dfile.encoding=UTF-8 -cp $LIBS pl.edu.pjwstk.p2pp.launchers.CommandLineLauncher $*
