package pl.edu.pjwstk.p2pp.debug.processor.subsystems;

/**
 * Interface used for producing {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}s.
 * See {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemWrapper} for further explanation.
 * 
 * @see {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
 *
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */

public interface SubSystemFactoryFunctor
{
    /**
     * Returns instance of {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
     * 
     * @return instance of {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
     */
    public SubSystem makeInstance();
}
