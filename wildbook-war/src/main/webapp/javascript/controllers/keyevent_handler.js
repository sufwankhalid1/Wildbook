//
// Code obtained from Ben Nadel from his blog
//    http://www.bennadel.com/blog/2816-managing-user-input-key-events-across-views-in-angularjs.htm
//
// ken: Added the generic link function.
//
var KeyEventHandler = (function() {
    // I bind the JavaScript events to the local scope.
    function link(setup, scope, element, attrs) {
        var watchScope;
        if (attrs.watchScope === "parent") {
            watchScope = scope.$parent;
        } else {
            watchScope = scope;
        }
        var keyHandler;
        // Handle this differently whether we are using ng-if or ng-show
        if (attrs.ngIf) {
            keyHandler = setup(scope, element);
            // teardown the key handler when the element is destroyed
            watchScope.$on(
                "$destroy",
                function() {
                    keyHandler.teardown();
                }
            );
        } else {
            // Setup a watch so that as the element is shown or hidden
            // we either recreate the keyhandler or tear it down.
            watchScope.$watch(attrs.ngShow, function(val) {
                //
                // If the variable we are watching is undefined then this is
                // a false call. Not sure what triggers that.
                if (val === undefined) {
                    return;
                }
                if (val) {
                    keyHandler = setup(scope, element);
                } else {
                    // Since we are listening for key events on a service (ie, not on
                    // the current Element), we have to be sure to teardown the bindings
                    // so that we don't get rogue event handlers persisting in the
                    // application.
                    if (keyHandler) {
                        keyHandler.teardown();
                        keyHandler == null;
                    }
                }
            });
        }
    }

    return {
        attach: function(app) {
            app.config(
                function keyEventsConfig(keyEventsProvider) {
                    // Each shortcut() operator gets called with the event object and the
                    // "is" object. The goal here is to add ".is" properties based on the
                    // state of the event.
                    keyEventsProvider.addShortcut(
                        function operator( event, is ) {
                            // NOTE: The ESC key is more consistently reported in the keydown
                            // event (as opposed to the keypress event). As such, we'll
                            // consider it false unless the event type is appropriate.
                            is.esc = ((event.type === "keydown") && (event.which === 27));
                            // NOTE: The CMD-ENTER combination is more consistently reported
                            // in the keydown event (as opposed to the keypress event). As
                            // such, we'll consider it false unless the event type is appropriate.
                            is.cmdEnter = ((event.type === "keydown") && (event.which === 13) && event.metaKey);
                            is.input = (
                                (event.target.tagName === "BUTTON") ||
                                (event.target.tagName === "INPUT") ||
                                (event.target.tagName === "SELECT") ||
                                (event.target.tagName === "TEXTAREA")
                            );
                            is.leftarrow = (event.which === 37);
                            is.rightarrow = (event.which === 39);
                        }
                    );
                }
            );
    
            app.provider(
                "keyEvents",
                function provideKeyEvents() {
                    // I hold the list of functions that can be used to decorate the .is.
                    // object with additional short-hand properties.
                    var shortcuts = [
                        function operator( event, is ) {
                            // Always add the lower-case key.
                            is[ String.fromCharCode( event.which ).toLowerCase() ] = true;
                        }
                    ];
                    // Return the provider API.
                    return({
                        // This is the core factory method for the service.
                        $get: keyEventsFactory,
                        // This is the provider-method that lets the developer add shortcut
                        // operators.
                        addShortcut: addShortcut
                    });
                    
                    // ---
                    // PUBLIC METHODS.
                    // ---
                    // I add a new shortcut operator that will be applied to every key event.
                    function addShortcut( operator ) {
                        shortcuts.push( operator );
                    }
                    // ---
                    // FACTORY METHOD.
                    // ---
                    // I provide the keyEvents service.
                    function keyEventsFactory( $document, KeyEventHandler ) {
                        // I hold the event handler queues in ascending priority order. As
                        // such, they should be iterated over in reverse order.
                        var eventHandlers = {
                                keydown: [],
                                keypress: [],
                                keyup: []
                        };
                        // Setup the public API.
                        var api = {
                            handler: handler,
                            off: off,
                            on: on
                        };
                        // Return the public API.
                        return( api );
                        
                        // ---
                        // PUBLIC METHODS.
                        // ---
                        // I create a new handler with the given priority. If the terminal
                        // flag is set, no handlers at a lower priority will receive the
                        // event, regardless of what this handler does to the event.
                        function handler( priority, isTerminal ) {
                            return( new KeyEventHandler( api, priority, ( isTerminal || false ) ) );
                        }
                        // I unbind the given event handler. This will unbind all matching
                        // handlers at any priority.
                        function off( eventType, handler ) {
                            deregisterHandler( eventType, handler );
                        }
                        // I bind a handler to the given event type at the given priority.
                        // If the terminal flag is set, no other handlers at a lower priority
                        // will receive the event.
                        function on( eventType, handler, priority, isTerminal ) {
                            registerHandler( eventType, handler, priority, isTerminal );
                        }
                        
                        // ---
                        // PRIVATE METHODS.
                        // ---
                        // I remove the given handler from the given event type.
                        function deregisterHandler( eventType, handler ) {
                            var handlers = eventHandlers[ eventType ];
                            for ( var i = ( handlers.length - 1 ) ; i >= 0 ; i-- ) {
                                if ( handlers[ i ].handler === handler ) {
                                    handlers.splice( i, 1 );
                                }
                            }
                            // If this particular event has no more event bindings, then
                            // stop watching for it at the root level.
                            if ( ! handlers.length ) {
                                stopWatchingEvent( eventType );
                            }
                        }
                        
                        // I process the root key-event, passing it through queue of event
                        // handlers implemented by the directives.
                        function handleEvent( event ) {
                            var handlers = eventHandlers[ event.type ];
                            event.is = {};
                            // Pass the event through the shortcut operators.
                            for ( var i = 0, length = shortcuts.length ; i < length ; i++ ) {
                                shortcuts[ i ]( event, event.is );
                            }
                            // Since the handlers are sorted in ascending priority, we need
                            // to process the queue in reverse order.
                            for ( var i = ( handlers.length - 1 ) ; i >= 0 ; i-- ) {
                                // If a handler returns an explicit false, kill the event.
                                if ( handlers[ i ].handler( event ) === false ) {
                                    event.stopImmediatePropagation();
                                    event.preventDefault();
                                    return( false );
                                }
                                // If the handler is flagged as terminal, or if the handle
                                // explicitly stopped propagation, don't pass the event onto
                                // any other handlers.
                                if ( handlers[ i ].isTerminal || event.isImmediatePropagationStopped() ) {
                                    break;
                                }
                            }
                        }
                        // I register the given event-type handler at the given priority.
                        function registerHandler( eventType, handler, priority, isTerminal ) {
                            var handlers = eventHandlers[ eventType ];
                            var newItem = {
                                handler: handler,
                                priority: priority,
                                isTerminal: isTerminal
                            };
                            // If it's the first handler, just push it and start watching for
                            // events on the document.
                            if ( ! handlers.length ) {
                                handlers.push( newItem );
                                return( startWatchingEvent( eventType ) );
                            }
                            // If it's the lowest priority handler, push it onto the bottom.
                            if ( handlers[ 0 ].priority > priority ) {
                                return( handlers.unshift( newItem ) );
                            }
                            // Otherwise, add it in priority sort order.
                            for ( var i = ( handlers.length - 1 ) ; i >= 0 ; i-- ) {
                                if ( handlers[ i ].priority <= priority ) {
                                    return( handlers.splice( ( i + 1 ), 0, newItem ) );
                                }
                            }
                        }
                        // I start watching the event event-type on the document.
                        function startWatchingEvent( eventType ) {
                            $document.on( eventType, handleEvent );
                        }
                        // I stop watching the event event-type on the document.
                        function stopWatchingEvent( eventType ) {
                            $document.off( eventType, handleEvent );
                        }
                    }
                }
            );
    
            // --------------------------------------------------------------------------- //
            // I work hand-in-hand with the keyEvents service to provide priority-specific
            // handler. This is just a convenience proxy to the keyEvents service.
            //--------------------------------------------------------------------------- //
            app.factory(
                "KeyEventHandler",
                function() {
                    // Return the constructor function.
                    return(KeyEventHandler);
                    
                    // I provide event-binding methods that are pre-bound to the given
                    // priority and terminal settings.
                    function KeyEventHandler(keyEvents, priority, isTerminal) {
                        // I hold the collection of event handlers associated.
                        var eventHandlers = {
                            keydown: [],
                            keypress: [],
                            keyup: []
                        };
                        // Setup the public API.
                        var api = {
                            keydown: keydown,
                            keypress: keypress,
                            keyup: keyup,
                            teardown: teardown
                        };
                        // Return the public API.
                        return( api );
                        // ---
                        // PUBLIC METHODS.
                        // ---
                        // I bind the given handler to the keydown event.
                        function keydown( handler ) {
                            eventHandlers.keydown.push( handler );
                            keyEvents.on( "keydown", handler, priority, isTerminal );
                            return( api );
                        }
                        // I bind the given handler to the keypress event.
                        function keypress( handler ) {
                            eventHandlers.keypress.push( handler );
                            keyEvents.on( "keypress", handler, priority, isTerminal );
                            return( api );
                        }
                        // I bind the given handler to the keyup event.
                        function keyup( handler ) {
                            eventHandlers.keyup.push( handler );
                            keyEvents.on( "keyup", handler, priority, isTerminal );
                            return( api );
                        }
                        // I unbind all of the event handlers bound by this proxy.
                        function teardown() {
                            teardownEventType( "keydown" );
                            teardownEventType( "keypress" );
                            teardownEventType( "keyup" );
                        }
                        // ---
                        // PRIVATE METHODS.
                        // ---
                        // I unbind the specific handlers from the core keyEvents service.
                        function teardownEventType( eventType ) {
                            var handlers = eventHandlers[ eventType ];
                            for ( var i = 0, length = handlers.length ; i < length ; i++ ) {
                                keyEvents.off( eventType, handlers[ i ] );
                            }
                        }
                    }
                }
            );
        },
        link: link
    };
})();