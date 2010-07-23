package net.customware.gwt.dispatch.server;

import java.util.List;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.BatchAction;
import net.customware.gwt.dispatch.shared.BatchResult;
import net.customware.gwt.dispatch.shared.DispatchException;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.dispatch.shared.ServiceException;
import net.customware.gwt.dispatch.shared.BatchAction.OnException;

/**
 * This handles {@link BatchAction} requests, which are a set of multiple
 * actions that need to all be executed successfully in sequence for the whole
 * action to succeed.
 * 
 * @author David Peterson
 */
public class BatchActionHandler extends AbstractActionHandler<BatchAction, BatchResult> {

    public BatchActionHandler() {
        super( BatchAction.class );
    }

    public BatchResult execute( BatchAction action, ExecutionContext context ) throws DispatchException {
        OnException onException = action.getOnException();
        List<Result> results = new java.util.ArrayList<Result>();
        List<DispatchException> exceptions = new java.util.ArrayList<DispatchException>();
        for ( Action<?> a : action.getActions() ) {
            Result result = null;
            try {
                result = context.execute( a );
                exceptions.add(null);
            } catch ( Exception e ) {
                DispatchException de = null;
                if ( e instanceof DispatchException )
                    de = ( DispatchException ) e;
                else
                    de = new ServiceException( e );

                if ( onException == OnException.ROLLBACK ) {
                    throw de;
                } else {
                    exceptions.add( de );
                }
            }
            results.add( result );
        }

        return new BatchResult( results, exceptions );
    }

    public void rollback( BatchAction action, BatchResult result, ExecutionContext context )
            throws ActionException {
        // No action necessary - the sub actions should automatically rollback
    }

}
