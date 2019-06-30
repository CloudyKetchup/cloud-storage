import React, {Component} from 'react';

import App from '../../../../../App';

export enum NotificationType {
	PROCESSING,
	ERROR
}

export type NotificationProps = {
	appContext 	: App
	id 			: number
	message?	: string
};

export abstract class Notification<T> extends Component<T> {

	icon = (icon : JSX.Element) => (
		<div style={{
		    float 		: 'left',
		    background 	: '#181818',
		    height 		: 'calc(100% - 20px)',
		    width 		: '50px',
		    padding 	: '10px',
		    textAlign 	: 'center',
		    fontSize 	: '25px'
		}}>
			{icon}
		</div>
	);

	backgroundOverlay = (key: number, appContext: App) => (
		<div className="notification-background-overlay" onClick={() => appContext.removeNotification(key)}>
			<i className="fas fa-trash-alt"/>
		</div>
	);

	message = (text: string) => (
		<div style={{
			width: 'calc(100% - 20px)',
			height: '100%'
		}}>
			<span style={{ lineHeight : '35px' }}>{text}</span>
		</div>
	);
}