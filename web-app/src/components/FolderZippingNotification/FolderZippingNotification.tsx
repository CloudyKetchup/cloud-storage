import React from 'react';

import {Notification} 		from '../Notification/Notification';
import {NotificationProps}	from '../Notification/Notification';
import CircularProgress 	from '@material-ui/core/CircularProgress';

interface FolderZippingNotificationProps extends NotificationProps {
	folderName 	: string
	processing 	: boolean
	error 		: boolean
}

export default class FolderZippingNotification extends Notification<FolderZippingNotificationProps> {
	state = { deleteToggler : false };

	componentDidMount = () => {
		const div = document.getElementById(`zipping-notification-${this.props.id}`);

		if (div !== null) div.addEventListener('contextmenu', this.toggleDeleteOption, false);
	};

	componentWillUnmount = () => {
		const div = document.getElementById(`zipping-notification-${this.props.id}`);

		if (div !== null) div.removeEventListener('contextmenu', this.toggleDeleteOption, false);
	};

	toggleDeleteOption = (e : Event) => {
		e.preventDefault();

		if (!this.props.processing) this.setState({ deleteToggler : !this.state.deleteToggler });
	};

	progressIcon = () => {
		if (this.props.error) {
			return this.processingError();
		} else if (this.props.processing) {
			return <CircularProgress className="circular-progress" size={30} style={{ color: '#ff723a' }}/>
		} else {
			return this.processingSuccess();
		}
	};

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
			<div id={`zipping-notification-${this.props.id}`} className="notification-body">
				{this.backgroundOverlay(this.props.id, this.props.appContext)}
				<div className="notification" style={{ marginLeft : this.state.deleteToggler ? '50px' : 0 }}>
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
			</div>
		);
	}
}