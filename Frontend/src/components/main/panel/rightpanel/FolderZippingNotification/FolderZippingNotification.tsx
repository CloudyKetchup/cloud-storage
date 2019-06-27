import React from 'react';

import {Notification}       from '../notification/Notification';
import CircularProgress     from '@material-ui/core/CircularProgress';

type FolderZippingNotificationProps = {
    folderName: string
    processing: boolean
    error: boolean
}

export default class FolderZippingNotification extends Notification<FolderZippingNotificationProps> {
    progressIcon = () => {
        if (this.props.error) {
            return this.processingError();
        } else if (this.props.processing) {
            return this.processingIcon();
        } else {
            return this.processingSuccess();
        }
    }

    processingIcon = () => (
        <CircularProgress className="circular-progress" size={30} style={{ color: '#ff723a' }}/>
    );

    processingError = () => (
        <div className="error-button">
            <i className="fas fa-times" style={{ lineHeight : '45px', marginRight : '10px' }}/>
        </div>
    );

    processingSuccess = () => (
        <div className="check-button">
            <i className="fas fa-check" style={{ lineHeight : '45px', marginRight : '10px' }}/>
        </div>
    );

    render() {
        return (
            <div className="notification">
                {this.icon(<i className="fas fa-file-archive"/>)}
                <div className="notification-content">
                    {this.message(`Zipping "${this.props.folderName}"`)}
                    <div style={{
	                    height: '40px',
	                    float: 'right'
                    }}>
                        {this.progressIcon()}
                    </div>
                </div>
            </div>
        );
    }
}