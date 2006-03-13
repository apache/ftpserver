// $Id:$
package org.apache.ftpserver.interfaces;

import org.apache.ftpserver.ftplet.Component;

/**
 * Command factory interface.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface ICommandFactory extends Component {

    /**
     * Get the command instance.
     */
    ICommand getCommand(String commandName);
}
