import React from 'react';

import {Notification}       from '../notification/Notification';
import CircularProgress     from '@material-ui/core/CircularProgress';

type FolderZippingNotificationProps = {
    folderName: string
    processing: boolean
}

export default class FolderZippingNotification extends Notification<FolderZippingNotificationProps> {
    
    render() {
        return (
            <div className="notification">
                {this.icon(<i className="fas fa-file-archive"/>)}
                <div className="notification-content">
                    {this.message(`Processing "${this.props.folderName}"`)}
                    <div style={{
	                    height: '40px',
	                    float: 'right'
                    }}>
                        {this.props.processing
                            ?   <CircularProgress className="circular-progress" size={30} style={{ color: '#ff723a' }}/>
                            :   <div className="check-button">
                                    <i className="fas fa-check" style={{ lineHeight : '45px', marginRight : '10px' }}/>
                                </div>}
                    </div>
                </div>
            </div>
        );
    }
}