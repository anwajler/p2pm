package pl.edu.pjwstk.p2pp.debug.processor.subsystems;

/**
 * Class providing a wrapper (or a delegator) for the {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
 * used for producing singleton {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}s able
 * to implement interfaces. Without such construction it would be impossible due to the way Java treats
 * static declarations. This is essentially a combination of the Abstract Factory, Factory Method
 * and Functor Pattern.
 *
 * @see {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
 * @see {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemFactoryFunctor}
 *
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */

final public class SubSystemWrapper {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Data members
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Instance of {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemFactoryFunctor}
     * used for producing {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem} instances.
     */
    static private SubSystemFactoryFunctor _factory = null;

    /**
     * Current instance of {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
     */
    static private SubSystem _instance = null;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Methods
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Accessor for the {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem} singleton.
     *
     * @return {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem} instance
     */
    static public synchronized SubSystem instance() {
        if (null == _instance) {
            _instance = (null == _factory) ? makeInstance() : _factory.makeInstance();
        }
        return _instance;
    }

    /**
     * Sets the factory used to create new {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
     * instances. Setting it null will cause a default method usage.
     *
     * @param factory Factory used to create {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}s
     */
    static public synchronized void setFactory(SubSystemFactoryFunctor factory) {
        _factory = factory;
    }

    /**
     * Sets the current {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem} instance.
     * Setting it null will cause a new instance to be created with a next {@link #instance()} call.
     *
     * @param instance {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem} instance to use
     */
    static public synchronized void setInstance(SubSystem instance) {
        _instance = instance;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Functions
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Default factory method called to create a new {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem}
     * when a new instance is needed and {@link #_factory} is null.
     *
     * @return {@link pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem} instance
     */
    static private SubSystem makeInstance() {
        return new SubSystem();
    }
    
}
