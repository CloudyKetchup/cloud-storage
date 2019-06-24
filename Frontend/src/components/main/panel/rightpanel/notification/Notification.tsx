import React, { Component } from 'react';

export interface NotificationProps {
	title: string;
	message: string;
}

export enum NotificationType {
	processing,
	error
} 

export abstract class Notification<T> extends Component<T> {

	icon = (icon : JSX.Element) => (
		<div style={{
			float: 'left',
			height: 'calc(100% - 20px)',
			width: '50px',
			padding: '10px',
			textAlign: 'center',
			fontSize: '25px'
		}}>
			{icon}
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