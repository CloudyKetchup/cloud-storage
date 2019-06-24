import React from 'react';

import {Notification, NotificationProps}    from "../notification/Notification";
import CircularProgress                     from '@material-ui/core/CircularProgress';

interface FolderZippingNotificationProps extends NotificationProps {
    folderName: string
    processing: boolean
}

export default class FolderZippingNotification extends Notification<FolderZippingNotificationProps> {
    render() {
        return (
            <div className="notification">
                {this.icon(<i className="fas fa-file-archive"/>)}
                <div style={{
                    display: 'flex',
	                float: 'right',
	                width: 'calc(-90px + 100%)',
	                height: 'calc(-20px + 100%)',
	                padding: '10px'
                }}>
                    {this.message(`Processing "${this.props.folderName}"`)}
                    <div style={{
	                    height: '40px',
	                    float: 'right'
                    }}>
                        {this.props.processing
                            ?   <CircularProgress size={20} style={{
                                    marginTop: '5px',
                                    marginRight : '5px',
	                                width: '30px',
	                                float: 'right',
                                    height: '30px',
                                    color: '#ff723a'
                                }}/>
                            :   <div style={{
                                    background: 'transparent',
                                    color: 'rgb(43, 206, 54)',
                                    fontSize: '20px',
                                    height: '100%'
                                }}>
                                    <i className="fas fa-check" style={{ lineHeight : '45px', marginRight : '10px' }}/>
                                </div>}
                    </div>
                </div>
            </div>
        );
    }
}