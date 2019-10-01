import React from 'react';

import {Notification} 		from '../Notification';
import {NotificationProps} 	from '../Notification';

interface Props extends NotificationProps {
	message: string
};

export default class ErrorNotification extends Notification<Props> {
	state = { deleteToggler : false };

	componentDidMount = () => {
		const div = document.getElementById(`error-notification-${this.props.id}`);

		if (div !== null) div.addEventListener('contextmenu', this.toggleDeleteOption, false);

		window.addEventListener('click', this.hideDeleteOption, false);
	};

	componentWillUnmount = () => {
		const div = document.getElementById(`error-notification-${this.props.id}`);

		if (div !== null) div.removeEventListener('contextmenu', this.toggleDeleteOption, false);

		window.removeEventListener('click', this.hideDeleteOption, false);
	};

	toggleDeleteOption = (e : Event) => {
		e.preventDefault();

		this.setState({ deleteToggler : !this.state.deleteToggler });
	};

	hideDeleteOption = (e : Event) => {
		this.setState({ deleteToggler : false });
	};

	render = () => (
		<div className="notification-body" id={`error-notification-${this.props.id}`}>
			{this.backgroundOverlay(this.props.id, this.props.appContext)}
			<div className="notification" style={{ marginLeft : this.state.deleteToggler ? '50px' : 0 }}>
				{this.icon(<i style={{ color : '#F32C2C' }} className="fas fa-exclamation-triangle"/>)}
				<div className="notification-content">
	                {this.message(this.props.message)}
	            </div>
            </div>
		</div>
	);
}