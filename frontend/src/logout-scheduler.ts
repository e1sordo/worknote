import api from "@/api/backend-api";
import { AxiosError } from 'axios';

// The request is periodically sent to the backend and depending on the work of the latter 
// the user session is either extended or a 403 error is returned and we refresh our application page to get the login window
const sendRequestToBackend = async () => {
    try {
        const response = await api.heartbeat()
        console.debug('Request successful:', response.status);
    } catch (error) {
        const axiosError = error as AxiosError;
        if (axiosError.response && axiosError.response.status === 403) {
            console.log('Request failed with 403. Reloading the page to make logout...');
        } else {
            console.log('Request failed with error:', axiosError.message);
        }
        window.location.reload();
    }
};

const interval = 15 * 60 * 1000;
setInterval(sendRequestToBackend, interval);
